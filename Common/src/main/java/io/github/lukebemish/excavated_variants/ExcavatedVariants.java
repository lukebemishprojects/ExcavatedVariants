package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.excavated_variants.api.IOreListModifier;
import io.github.lukebemish.excavated_variants.client.ClientServices;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.data.ModConfig;
import io.github.lukebemish.excavated_variants.data.ModData;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
import io.github.lukebemish.excavated_variants.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.DynAssetGeneratorServerAPI;
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

public class ExcavatedVariants {
    public static final String MOD_ID = "excavated_variants";

    public static final IMainPlatformTarget MAIN_PLATFORM_TARGET = Services.load(IMainPlatformTarget.class);
    public static final Supplier<RecipeSerializer<OreConversionRecipe>> ORE_CONVERSION = MAIN_PLATFORM_TARGET.registerRecipeSerializer("ore_conversion",()->
            new SimpleRecipeSerializer<>(OreConversionRecipe::new));

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList;

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
                String full_id = stone.id+"_"+ore.id;
                blockList.add(new RegistryFuture(full_id,ore, stone));
                if (getConfig().add_conversion_recipes) {
                    OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, full_id), ore.block_id.get(0));
                }
                blockTagBuilder.add(new ResourceLocation(ExcavatedVariants.MOD_ID,full_id));
                stoneTag.add(full_id, ore);
                ironTag.add(full_id, ore);
                diamondTag.add(full_id, ore);
                ids.add(full_id);
            }
            for (String orename : ore.orename) {
                for (String this_id : ids) {
                    String oreTypeName = orename.substring(0, orename.length() - 4);
                    if (Services.PLATFORM.isFabriquilt()) {
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
        if (!Services.PLATFORM.isFabriquilt()) {
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

        MAIN_PLATFORM_TARGET.registerFeatures();

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
        Map<String, BaseStone> stoneMap = new HashMap<>();
        Map<String, List<BaseOre>> oreMap = new HashMap<>();
        for (ModData mod : ExcavatedVariants.getConfig().mods) {
            if (modids.containsAll(mod.mod_id)) {
                for (BaseStone stone : mod.provided_stones) {
                    if (!ExcavatedVariants.getConfig().blacklist_stones.contains(stone.id)) {
                        stoneMap.put(stone.id, stone);
                    }
                }
                for (BaseOre ore : mod.provided_ores) {
                    if (!ExcavatedVariants.getConfig().blacklist_ores.contains(ore.id)) {
                        oreMap.computeIfAbsent(ore.id, k -> new ArrayList<>());
                        oreMap.get(ore.id).add(ore);
                    }
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
                List<String> orenames = new HashSet<>(pair.first().orename).stream().toList();
                pair.first().orename.clear();
                pair.first().orename.addAll(orenames);
            }
            oreStoneList.add(pair);
            for (BaseStone stone : stoneMap.values()) {
                if (!stones.contains(stone.id) && oreList.stream().anyMatch(x->x.types.stream().anyMatch(stone.types::contains))) {
                    String full_id = stone.id + "_" + id;
                    if (!ExcavatedVariants.getConfig().blacklist_ids.contains(full_id)) {
                        pair.last().add(stone);
                    }
                }
            }
        }
        var listListeners = Services.COMPAT.getOreListModifiers();
        for (IOreListModifier listListener : listListeners) {
            oreStoneList = listListener.modify(oreStoneList,stoneMap.values());
        }

        HashSet<String> done_ids = new HashSet<>();
        ArrayList<Pair<BaseOre,HashSet<BaseStone>>> out = new ArrayList<>();
        for (Pair<BaseOre,HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.first();
            if (!done_ids.contains(ore.id)) {
                done_ids.add(ore.id);
                Pair<BaseOre,HashSet<BaseStone>> o = new Pair<>(ore,new HashSet<>());
                out.add(o);
                for (BaseStone stone : p.last()) {
                    String full_id = stone.id + "_" + ore.id;
                    if (!ExcavatedVariants.getConfig().blacklist_ids.contains(full_id)) {
                        o.last().add(stone);
                    }
                }
            }
        }
        oreStoneList = out;
    }

    private static ModConfig configs;

    public static ModConfig getConfig() {
        if (configs == null) {
            configs = ModConfig.get();
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
        public Boolean done = false;
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
            ModifiedOreBlock b = new ModifiedOreBlock(o, s);
            blockRegistrar.accept(rlToReg, b);
            blocks.put(id, b);
            Supplier<Item> i = itemRegistrar.apply(rlToReg, ()->new BlockItem(b, new Item.Properties().tab(Services.CREATIVE_TAB_LOADER.getCreativeTab())));
            items.add(i);

            ClientServices.RENDER_TYPE_HANDLER.setRenderTypeMipped(b);
        }
    }

    public static List<Supplier<Item>> getItems() {
        return items;
    }

    private static final List<Supplier<Item>> items = new ArrayList<>();
    private static final Map<String, ModifiedOreBlock> blocks = new HashMap<>();
    private static final List<RegistryFuture> blockList = new ArrayList<>();


    public static ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED;
    public static PlacedFeature ORE_REPLACER_PLACED;

    private static boolean loaded = false;

    public static boolean hasLoaded() {
        return loaded;
    }

    public static List<ResourceLocation> loadedBlockRLs = new ArrayList<>();
}
