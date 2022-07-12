package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.dynamic_asset_generator.api.DynAssetGeneratorServerAPI;
import io.github.lukebemish.excavated_variants.api.IOreListModifier;
import io.github.lukebemish.excavated_variants.client.ClientServices;
import io.github.lukebemish.excavated_variants.data.*;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
import io.github.lukebemish.excavated_variants.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExcavatedVariants {
    public static final String MOD_ID = "excavated_variants";

    public static final Supplier<RecipeSerializer<OreConversionRecipe>> ORE_CONVERSION = Services.MAIN_PLATFORM_TARGET.get().registerRecipeSerializer("ore_conversion",()->
            new SimpleRecipeSerializer<>(OreConversionRecipe::new));

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList;
    private static Set<BaseOre> knownOres;
    private static Set<BaseStone> knownStones;

    public static void init() {
        setupMap();
        List<ResourceLocation> blockTagBuilder = new ArrayList<>();
        MiningLevelTagGenerator stoneTag = new MiningLevelTagGenerator("stone");
        MiningLevelTagGenerator ironTag = new MiningLevelTagGenerator("iron");
        MiningLevelTagGenerator diamondTag = new MiningLevelTagGenerator("diamond");

        ExcavatedVariants.setupMap();
        for (Pair<BaseOre,HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.first();
            List<String> ids = new ArrayList<>();
            for (BaseStone stone : p.last()) {
                String fullId = stone.id+"_"+ore.id;
                blockList.add(new RegistryFuture(fullId,ore, stone));
                neededRls.add(ore.block_id.get(0));
                neededRls.add(stone.block_id);
                if (getConfig().addConversionRecipes) {
                    OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, fullId), ore.block_id.get(0));
                }
                blockTagBuilder.add(new ResourceLocation(ExcavatedVariants.MOD_ID,fullId));
                stoneTag.add(fullId, ore);
                ironTag.add(fullId, ore);
                diamondTag.add(fullId, ore);
                ids.add(fullId);
            }
            for (String orename : ore.orename) {
                for (String this_id : ids) {
                    String oreTypeName = orename.substring(0, orename.length() - 4);
                    if (Services.PLATFORM.isQuilt()) {
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c", "items/" + orename + "s"),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c", "blocks/" + orename + "s"),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c","items/ores/"+oreTypeName),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c","blocks/ores/"+oreTypeName),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                    } else {
                        if (orename.endsWith("_ore")) {
                            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("forge","items/ores/"+oreTypeName),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("forge","blocks/ores/"+oreTypeName),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                        }
                    }
                    if (Arrays.asList("iron_ore", "gold_ore", "coal_ore", "emerald_ore", "diamond_ore", "redstone_ore", "quartz_ore", "copper_ore", "netherite_scrap_ore").contains(orename)) {
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("minecraft", "items/" + orename + "s"),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("minecraft", "blocks/" + orename + "s"),new ResourceLocation(ExcavatedVariants.MOD_ID,this_id));
                    }
                }
            }
        }

        DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("minecraft", "blocks/mineable/pickaxe"),blockTagBuilder);
        if (!Services.PLATFORM.isQuilt()) {
            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("forge", "blocks/ores"),blockTagBuilder);
            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("forge", "items/ores"),blockTagBuilder);
        } else {
            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c", "blocks/ores"),blockTagBuilder);
            DynAssetGeneratorServerAPI.planTagFile(new ResourceLocation("c", "items/ores"),blockTagBuilder);
        }

        DynAssetGeneratorServerAPI.planTagFileConditional(new ResourceLocation("minecraft","blocks/needs_stone_tool"),
                stoneTag.suppliers());
        DynAssetGeneratorServerAPI.planTagFileConditional(new ResourceLocation("minecraft","blocks/needs_iron_tool"),
                ironTag.suppliers());
        DynAssetGeneratorServerAPI.planTagFileConditional(new ResourceLocation("minecraft","blocks/needs_diamond_tool"),
                diamondTag.suppliers());

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();

        loaded = true;
    }

    public static boolean setupMap() {
        if (oreStoneList == null || oreStoneList.size() == 0) {
            Collection<String> modids;
            try {
                modids = Services.PLATFORM.getModIds();
            } catch (NullPointerException e) {
                //no need to lock up the class, but we have to guarantee that oreStoneList is NonNull after this.
                oreStoneList = new ArrayList<>();
                return false;
            }
            internalSetupMap(modids);
        }
        return true;
    }

    private static synchronized void internalSetupMap(Collection<String> modids) {
        // Yeah, yeah, I don't like static synchronized either. This way, though, it should only ever fire once, since
        // this is an internal method and only ever locks if the list is null or empty. And I don't really want to
        // build the list more than once, since that causes issues, so...
        oreStoneList = new ArrayList<>();
        knownOres = new HashSet<>();
        knownStones = new HashSet<>();
        Map<String, BaseStone> stoneMap = new HashMap<>();
        Map<String, List<BaseOre>> oreMap = new HashMap<>();
        for (ModData mod : ExcavatedVariants.getConfig().mods) {
            if (modids.containsAll(mod.mod_id)) {
                for (BaseStone stone : mod.provided_stones) {
                    if (!stoneMap.containsKey(stone.id)) stoneMap.put(stone.id, stone);
                    else {
                        BaseStone stoneOld = stoneMap.get(stone.id);
                        List<String> types = new ArrayList<>(stoneOld.types);
                        types.addAll(stone.types.stream().filter(s->!stoneOld.types.contains(s)).toList());
                        stoneOld.types = types;
                    }
                }
                for (BaseOre ore : mod.provided_ores) {
                    oreMap.computeIfAbsent(ore.id, k -> new ArrayList<>());
                    oreMap.get(ore.id).add(ore);
                }
            }
        }
        for (String id : oreMap.keySet()) {
            List<BaseOre> oreList = oreMap.get(id);
            List<String> stones = new ArrayList<>();
            for (BaseOre ore : oreList) {
                stones.addAll(ore.stone);
            }
            Pair<BaseOre, HashSet<BaseStone>> pair = new Pair<>(oreList.get(0).clone(), new HashSet<>());
            if (oreList.size() > 1) {
                pair.first().block_id = new ArrayList<>();
                pair.first().orename = new ArrayList<>();
                pair.first().stone = new ArrayList<>();
                pair.first().types = new ArrayList<>();
                for (BaseOre baseOre : oreList) {
                    pair.first().block_id.addAll(baseOre.block_id);
                    pair.first().orename.addAll(baseOre.orename);
                    pair.first().stone.addAll(baseOre.stone);
                    pair.first().types.addAll(baseOre.types);
                }
                List<String> types = new HashSet<>(pair.first().types).stream().toList();
                pair.first().types.clear();
                pair.first().types.addAll(types);
                List<String> oreNames = new HashSet<>(pair.first().orename).stream().toList();
                pair.first().orename.clear();
                pair.first().orename.addAll(oreNames);
            }
            oreStoneList.add(pair);
            for (BaseStone stone : stoneMap.values()) {
                if (!stones.contains(stone.id) && oreList.stream().anyMatch(x->x.types.stream().anyMatch(stone.types::contains))) {
                    if (!ExcavatedVariants.getConfig().configResource.blacklist.matches(id, stone.id)) {
                        pair.last().add(stone);
                    }
                }
            }
        }
        var listListeners = Services.COMPAT.getOreListModifiers();
        for (IOreListModifier listListener : listListeners) {
            oreStoneList = listListener.modify(oreStoneList,stoneMap.values());
        }

        HashSet<String> doneIds = new HashSet<>();
        ArrayList<Pair<BaseOre,HashSet<BaseStone>>> out = new ArrayList<>();
        for (Pair<BaseOre,HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.first();
            if (!doneIds.contains(ore.id)) {
                doneIds.add(ore.id);
                Pair<BaseOre,HashSet<BaseStone>> o = new Pair<>(ore,new HashSet<>());
                out.add(o);
                knownOres.add(o.first());
                for (BaseStone stone : p.last()) {
                    if (!ExcavatedVariants.getConfig().configResource.blacklist.matches(ore, stone)) {
                        o.last().add(stone);
                    }
                }
                knownStones.addAll(o.last());
            }
        }
        oreStoneList = out;
    }

    private static ModConfig configs;

    public static ModConfig getConfig() {
        if (configs == null) {
            configs = ModConfig.load();
        }
        return configs;
    }

    public static Map<String, ModifiedOreBlock> getBlocks() {
        return blocks;
    }

    public static List<RegistryFuture> getBlockList() {
        return blockList;
    }

    public static class RegistryFuture {
        public final BaseOre ore;
        public final BaseStone stone;
        public final String full_id;
        public boolean done = false;
        public RegistryFuture(String s, BaseOre ore, BaseStone stone) {
            this.full_id = s;
            this.ore = ore;
            this.stone = stone;
        }
    }

    public static void registerBlockAndItem(BiConsumer<ResourceLocation,Block> blockRegistrar, BiFunction<ResourceLocation,Supplier<Item>,Supplier<Item>> itemRegistrar, RegistryFuture future) {
        if (!future.done) {
            future.done = true;
            String id = future.full_id;
            BaseOre o = future.ore;
            BaseStone s = future.stone;
            ResourceLocation rlToReg = new ResourceLocation(ExcavatedVariants.MOD_ID, future.full_id);
            ModifiedOreBlock.setupStaticWrapper(o, s);
            ModifiedOreBlock b = Services.MAIN_PLATFORM_TARGET.get().makeDefaultOreBlock(o, s);
            blockRegistrar.accept(rlToReg, b);
            blocks.put(id, b);
            Supplier<Item> i = itemRegistrar.apply(rlToReg, ()->new BlockItem(b, new Item.Properties().tab(Services.CREATIVE_TAB_LOADER.get().getCreativeTab())));
            items.add(i);

            ClientServices.RENDER_TYPE_HANDLER.setRenderTypeMipped(b);
        }
    }

    public static List<Supplier<Item>> getItems() {
        return items;
    }

    private static final List<Supplier<Item>> items = new ArrayList<>();
    private static final Map<String, ModifiedOreBlock> blocks = new HashMap<>();
    public static final List<RegistryFuture> blockList = new ArrayList<>();
    public static final Set<ResourceLocation> neededRls = new HashSet<>();

    public static ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED;
    public static PlacedFeature ORE_REPLACER_PLACED;

    private static boolean loaded = false;

    public static boolean hasLoaded() {
        return loaded;
    }

    public static Set<ResourceLocation> loadedBlockRLs = new HashSet<>();

    private static MappingsCache mappingsCache;

    public static synchronized MappingsCache getMappingsCache() {
        if (mappingsCache==null && setupMap()) {
            MappingsCache cache = MappingsCache.load();
            knownOres.forEach(ore -> cache.oreMappings.put(ore.id, Set.copyOf(ore.block_id)));
            knownStones.forEach(stone -> cache.stoneMappings.put(stone.id, stone.block_id));

            Map<String, Set<ResourceLocation>> newOres = new HashMap<>();
            Map<String, ResourceLocation> newStones = new HashMap<>();
            cache.oreMappings.forEach((key,rls) -> {
                Set<ResourceLocation> set = rls.stream().filter(it->Services.REGISTRY_UTIL.getBlockById(it)!=null).collect(Collectors.toUnmodifiableSet());
                if (!set.isEmpty()) newOres.put(key, set);
            });
            cache.stoneMappings.forEach((key,rl) -> {
                if (Services.REGISTRY_UTIL.getBlockById(rl)!=null) newStones.put(key,rl);
            });

            cache.oreMappings = newOres;
            cache.stoneMappings = newStones;

            cache.save();

            mappingsCache = cache;
        }
        return mappingsCache;
    }
}
