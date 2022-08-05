package io.github.lukebemish.excavated_variants;

import com.google.common.base.Functions;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.DataResourceCache;
import io.github.lukebemish.excavated_variants.api.IOreListModifier;
import io.github.lukebemish.excavated_variants.client.ClientServices;
import io.github.lukebemish.excavated_variants.data.*;
import io.github.lukebemish.excavated_variants.data.filter.Filter;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
import net.minecraft.core.Registry;
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

public final class ExcavatedVariants {
    private ExcavatedVariants() {}

    public static final String MOD_ID = "excavated_variants";

    public static final Supplier<RecipeSerializer<OreConversionRecipe>> ORE_CONVERSION = Services.MAIN_PLATFORM_TARGET.get().registerRecipeSerializer("ore_conversion", () ->
            new SimpleRecipeSerializer<>(OreConversionRecipe::new));

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final List<RegistryFuture> blockList = new ArrayList<>();
    public static final Set<ResourceLocation> neededRls = new HashSet<>();
    private static final List<Supplier<Item>> items = new ArrayList<>();
    private static final Map<String, ModifiedOreBlock> blocks = new HashMap<>();
    public static List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList;
    public static ConfiguredFeature<NoneFeatureConfiguration, ?> ORE_REPLACER_CONFIGURED;
    public static PlacedFeature ORE_REPLACER_PLACED;
    public static Set<ResourceLocation> loadedBlockRLs = new HashSet<>();
    private static Set<BaseOre> knownOres;
    private static Set<BaseStone> knownStones;
    private static Map<String, BaseOre> ores;
    private static Map<String, BaseStone> stones;
    private static Set<Pair<BaseOre, BaseStone>> allPairs;
    private static ModConfig configs;
    private static boolean loaded = false;
    private static MappingsCache mappingsCache;

    public static void init() {
        setupMap();
        MiningLevelTagGenerator stoneTag = new MiningLevelTagGenerator("stone");
        MiningLevelTagGenerator ironTag = new MiningLevelTagGenerator("iron");
        MiningLevelTagGenerator diamondTag = new MiningLevelTagGenerator("diamond");

        for (Pair<BaseOre, HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.getFirst();
            List<String> ids = new ArrayList<>();
            for (BaseStone stone : p.getSecond()) {
                String fullId = stone.id + "_" + ore.id;
                blockList.add(new RegistryFuture(fullId, ore, stone));
                neededRls.add(ore.block_id.get(0));
                neededRls.add(stone.block_id);
                if (getConfig().addConversionRecipes) {
                    OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, fullId), ore.block_id.get(0));
                }
                for (String type : Sets.union(new HashSet<>(stone.types), new HashSet<>(ore.types))) {
                    ResourceLocation tagRl = new ResourceLocation(MOD_ID, type + "_ores");
                    ResourceLocation id = new ResourceLocation(MOD_ID, fullId);
                    planItemTag(rlToBlock(tagRl), id);
                    planBlockTag(rlToItem(tagRl), id);
                }
                stoneTag.add(fullId, ore);
                ironTag.add(fullId, ore);
                diamondTag.add(fullId, ore);
                ids.add(fullId);
            }
            for (String orename : ore.orename) {
                for (String this_id : ids) {
                    String oreTypeName = orename.substring(0, orename.length() - 4);
                    if (Services.PLATFORM.isQuilt()) {
                        planItemTag(new ResourceLocation("c", "items/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        planBlockTag(new ResourceLocation("c", "blocks/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        planItemTag(new ResourceLocation("c", "items/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        planBlockTag(new ResourceLocation("c", "blocks/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                    } else {
                        if (orename.endsWith("_ore")) {
                            planItemTag(new ResourceLocation("forge", "items/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                            planBlockTag(new ResourceLocation("forge", "blocks/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        }
                    }
                    if (Arrays.asList("iron_ore", "gold_ore", "coal_ore", "emerald_ore", "diamond_ore", "redstone_ore", "quartz_ore", "copper_ore", "netherite_scrap_ore").contains(orename)) {
                        planItemTag(new ResourceLocation("minecraft", "items/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        planBlockTag(new ResourceLocation("minecraft", "blocks/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                    }
                }
            }
        }

        getConfig().modifiers.forEach(modifier -> {
            List<ResourceLocation> tags;
            if (modifier.tags().isPresent() && !(tags = modifier.tags().get()).isEmpty()) {
                List<ResourceLocation> locations = getMatching(modifier.filter()).stream().map(p ->
                        new ResourceLocation(ExcavatedVariants.MOD_ID, p.getSecond().id + "_" + p.getFirst().id)).toList();
                tags.forEach(tag -> locations.forEach(rl -> {
                    if (tag.getPath().startsWith("blocks/"))
                        planBlockTag(tag, rl);
                    else if (tag.getPath().startsWith("items/"))
                        planItemTag(tag, rl);
                }));
            }
        });

        for (BaseStone stone : knownStones) {
            for (String type : stone.types) {
                ResourceLocation tagRl = new ResourceLocation(MOD_ID, type + "_stones");
                planBlockTag(rlToBlock(tagRl), stone.block_id);
                planItemTag(rlToItem(tagRl), stone.block_id);
            }
        }

        for (BaseOre ore : knownOres) {
            for (String type : ore.types) {
                ResourceLocation tagRl = new ResourceLocation(MOD_ID, type + "_ores");
                ore.block_id.forEach(id -> {
                    planItemTag(rlToBlock(tagRl), id);
                    planBlockTag(rlToItem(tagRl), id);
                });
            }
        }

        DataResourceCache.INSTANCE.planTag(new ResourceLocation("minecraft", "blocks/needs_stone_tool"), stoneTag);
        DataResourceCache.INSTANCE.planTag(new ResourceLocation("minecraft", "blocks/needs_iron_tool"), ironTag);
        DataResourceCache.INSTANCE.planTag(new ResourceLocation("minecraft", "blocks/needs_diamond_tool"), diamondTag);

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();

        loaded = true;
    }

    // Prior to map setup, filters *must* be called using the BaseOre and BaseStone, not the string IDs.
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

    public static Set<Pair<BaseOre, BaseStone>> getMatching(Filter filter) {
        setupMap();
        return allPairs.stream().filter(p -> filter.matches(p.getFirst(), p.getSecond())).collect(Collectors.toSet());
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
            if (modids.containsAll(mod.modId)) {
                for (BaseStone stone : mod.providedStones) {
                    if (!stoneMap.containsKey(stone.id)) stoneMap.put(stone.id, stone);
                    else {
                        BaseStone stoneOld = stoneMap.get(stone.id);
                        List<String> types = new ArrayList<>(stoneOld.types);
                        types.addAll(stone.types.stream().filter(s -> !stoneOld.types.contains(s)).toList());
                        stoneOld.types = types;
                    }
                }
                for (BaseOre ore : mod.providedOres) {
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
                pair.getFirst().block_id = new ArrayList<>();
                pair.getFirst().orename = new ArrayList<>();
                pair.getFirst().stone = new ArrayList<>();
                pair.getFirst().types = new ArrayList<>();
                for (BaseOre baseOre : oreList) {
                    pair.getFirst().block_id.addAll(baseOre.block_id);
                    pair.getFirst().orename.addAll(baseOre.orename);
                    pair.getFirst().stone.addAll(baseOre.stone);
                    pair.getFirst().types.addAll(baseOre.types);
                }
                List<String> types = new HashSet<>(pair.getFirst().types).stream().toList();
                pair.getFirst().types.clear();
                pair.getFirst().types.addAll(types);
                List<String> oreNames = new HashSet<>(pair.getFirst().orename).stream().toList();
                pair.getFirst().orename.clear();
                pair.getFirst().orename.addAll(oreNames);
            }
            oreStoneList.add(pair);
            for (BaseStone stone : stoneMap.values()) {
                if (!stones.contains(stone.id) && pair.getFirst().types.stream().anyMatch(stone.types::contains)) {
                    if (!ExcavatedVariants.getConfig().configResource.getBlacklist().matches(pair.getFirst(), stone)) {
                        pair.getSecond().add(stone);
                    }
                }
            }
        }
        var listListeners = Services.COMPAT.getOreListModifiers();
        for (IOreListModifier listListener : listListeners) {
            oreStoneList = listListener.modify(oreStoneList, stoneMap.values());
        }

        HashSet<String> doneIds = new HashSet<>();
        ArrayList<Pair<BaseOre, HashSet<BaseStone>>> out = new ArrayList<>();
        for (Pair<BaseOre, HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.getFirst();
            if (!doneIds.contains(ore.id)) {
                doneIds.add(ore.id);
                Pair<BaseOre, HashSet<BaseStone>> o = new Pair<>(ore, new HashSet<>());
                out.add(o);
                knownOres.add(o.getFirst());
                for (BaseStone stone : p.getSecond()) {
                    if (!ExcavatedVariants.getConfig().configResource.getBlacklist().matches(ore, stone)) {
                        o.getSecond().add(stone);
                    }
                }
                knownStones.addAll(o.getSecond());
            }
        }
        stones = knownStones.stream().collect(Collectors.toMap(s -> s.id, Functions.identity()));
        ores = knownOres.stream().collect(Collectors.toMap(o -> o.id, Functions.identity()));
        allPairs = out.stream().flatMap(p -> p.getSecond().stream().map(o -> new Pair<>(p.getFirst(), o))).collect(Collectors.toSet());
        oreStoneList = out;
    }

    public static Map<String, BaseOre> getOres() {
        return ores;
    }

    public static Map<String, BaseStone> getStones() {
        return stones;
    }

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

    public static void registerBlockAndItem(BiConsumer<ResourceLocation, Block> blockRegistrar, BiFunction<ResourceLocation, Supplier<Item>, Supplier<Item>> itemRegistrar, RegistryFuture future) {
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
            Supplier<Item> i = itemRegistrar.apply(rlToReg, () -> new BlockItem(b, new Item.Properties().tab(Services.CREATIVE_TAB_LOADER.get().getCreativeTab())));
            items.add(i);

            ClientServices.RENDER_TYPE_HANDLER.setRenderTypeMipped(b);
        }
    }

    public static List<Supplier<Item>> getItems() {
        return items;
    }

    public static boolean hasLoaded() {
        return loaded;
    }

    public static synchronized MappingsCache getMappingsCache() {
        if (mappingsCache == null && setupMap()) {
            MappingsCache cache = MappingsCache.load();
            knownOres.forEach(ore -> cache.oreMappings.put(ore.id, Set.copyOf(ore.block_id)));
            knownStones.forEach(stone -> cache.stoneMappings.put(stone.id, stone.block_id));

            Map<String, Set<ResourceLocation>> newOres = new HashMap<>();
            Map<String, ResourceLocation> newStones = new HashMap<>();
            cache.oreMappings.forEach((key, rls) -> {
                Set<ResourceLocation> set = rls.stream().filter(it -> Services.REGISTRY_UTIL.getBlockById(it) != null).collect(Collectors.toUnmodifiableSet());
                if (!set.isEmpty()) newOres.put(key, set);
            });
            cache.stoneMappings.forEach((key, rl) -> {
                if (Services.REGISTRY_UTIL.getBlockById(rl) != null) newStones.put(key, rl);
            });

            cache.oreMappings = newOres;
            cache.stoneMappings = newStones;

            cache.save();

            mappingsCache = cache;
        }
        return mappingsCache;
    }

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block) {
        DataResourceCache.INSTANCE.planTag(tag, new Pair<>(block, () -> Registry.BLOCK.containsKey(block)));
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item) {
        DataResourceCache.INSTANCE.planTag(tag, new Pair<>(item, () -> Registry.ITEM.containsKey(item)));
    }

    private static ResourceLocation rlToBlock(ResourceLocation rl) {
        return new ResourceLocation(rl.getNamespace(), "blocks/" + rl.getPath());
    }

    private static ResourceLocation rlToItem(ResourceLocation rl) {
        return new ResourceLocation(rl.getNamespace(), "items/" + rl.getPath());
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
}
