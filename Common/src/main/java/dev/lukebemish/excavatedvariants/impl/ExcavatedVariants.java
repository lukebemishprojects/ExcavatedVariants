package dev.lukebemish.excavatedvariants.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import blue.endless.jankson.Jankson;
import com.google.common.base.Functions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache;
import dev.lukebemish.excavatedvariants.api.DataProvider;
import dev.lukebemish.excavatedvariants.api.DataReceiver;
import dev.lukebemish.excavatedvariants.api.IOreListModifier;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.client.ClientServices;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import dev.lukebemish.excavatedvariants.impl.data.MappingsCache;
import dev.lukebemish.excavatedvariants.impl.data.ModConfig;
import dev.lukebemish.excavatedvariants.impl.data.ModData;
import dev.lukebemish.excavatedvariants.impl.data.filter.Filter;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import dev.lukebemish.excavatedvariants.impl.recipe.OreConversionRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;

public final class ExcavatedVariants {
    public static final int DEFAULT_COMPAT_PRIORITY = -10;
    public static final Jankson JANKSON = Jankson.builder().build();

    private ExcavatedVariants() {}
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    public static final Gson GSON_CONDENSED = new GsonBuilder().setLenient().create();

    public static final String MOD_ID = "excavated_variants";

    public static final Supplier<RecipeSerializer<OreConversionRecipe>> ORE_CONVERSION = Services.MAIN_PLATFORM_TARGET.get().registerRecipeSerializer("ore_conversion", () ->
            new SimpleCraftingRecipeSerializer<>(OreConversionRecipe::new));

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final List<RegistryFuture> blockList = new ArrayList<>();
    public static final Set<ResourceLocation> neededRls = new HashSet<>();
    private static final List<Supplier<Item>> items = new ArrayList<>();
    private static final Map<String, ModifiedOreBlock> blocks = new HashMap<>();
    public static List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList;
    public static Set<ResourceLocation> loadedBlockRLs = new HashSet<>();
    private static Set<BaseOre> knownOres;
    private static Set<BaseStone> knownStones;
    private static Map<String, BaseOre> ores;
    private static Map<String, BaseStone> stones;
    private static Set<Pair<BaseOre, BaseStone>> allPairs;
    private static ModConfig configs;
    private static boolean loaded = false;
    private static MappingsCache mappingsCache;

    public static final DataResourceCache DATA_CACHE = ResourceCache.register(new DataResourceCache(new ResourceLocation(MOD_ID, "data")));

    private static void planTagLang(ResourceLocation rl) {
        if (Services.PLATFORM.isClient()) {
            String path = rl.getPath();
            if (path.startsWith("items/")) {
                path = path.replaceFirst("items/","");
            } else if (path.startsWith("blocks/")) {
                path = path.replaceFirst("blocks/","");
            }
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(path.split("/")));
            Collections.reverse(parts);
            String englishName = String.join(" ",parts.stream().flatMap(s -> Arrays.stream(s.split("_")))
                    .map(s -> {
                        if (!s.isEmpty()) {
                            return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1);
                        }
                        return s;
                    })
                    .toList());
            String tagName = "tag."+rl.getNamespace()+"."+String.join(".",Arrays.asList(path.split("/")));
            ExcavatedVariantsClient.planLang(tagName, englishName);
        }
    }

    public static void init() {
        setupMap();

        MiningLevelTagHolder tierHolder = new MiningLevelTagHolder();

        for (Pair<BaseOre, HashSet<BaseStone>> p : oreStoneList) {
            BaseOre ore = p.getFirst();
            List<String> ids = new ArrayList<>();
            for (BaseStone stone : p.getSecond()) {
                String fullId = stone.id + "_" + ore.id;
                blockList.add(new RegistryFuture(fullId, ore, stone));
                neededRls.add(ore.blockId.get(0));
                neededRls.add(stone.blockId);
                if (getConfig().addConversionRecipes) {
                    OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, fullId), ore.blockId.get(0));
                }
                for (String type : Sets.union(new HashSet<>(stone.types), new HashSet<>(ore.types))) {
                    ResourceLocation tagRl = new ResourceLocation(MOD_ID, type + "_ores");
                    ResourceLocation id = new ResourceLocation(MOD_ID, fullId);
                    planItemTag(rlToBlock(tagRl), id);
                    planBlockTag(rlToItem(tagRl), id);
                }
                tierHolder.add(fullId, ore, stone);
                ids.add(fullId);
            }
            for (String orename : ore.oreName) {
                for (String this_id : ids) {
                    String oreTypeName = orename.substring(0, orename.length() - 4);
                    if (Services.PLATFORM.isQuilt()) {
                        planItemTag(new ResourceLocation("c", "items/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id), true);
                        planBlockTag(new ResourceLocation("c", "blocks/" + orename + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        planItemTag(new ResourceLocation("c", "items/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id), true);
                        planBlockTag(new ResourceLocation("c", "blocks/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                    } else {
                        if (orename.endsWith("_ore")) {
                            planItemTag(new ResourceLocation("forge", "items/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id), true);
                            planBlockTag(new ResourceLocation("forge", "blocks/ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, this_id));
                        }
                    }
                    if (Arrays.asList("iron_ore", "gold_ore", "coal_ore", "emerald_ore", "diamond_ore", "redstone_ore", "quartz_ore", "copper_ore", "lapis_ore", "netherite_scrap_ore").contains(orename)) {
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
                planItemTag(rlToItem(tagRl), stone.blockId, true);
                planBlockTag(rlToBlock(tagRl), stone.blockId);
            }
        }

        for (BaseOre ore : knownOres) {
            for (String type : ore.types) {
                ResourceLocation tagRl = new ResourceLocation(MOD_ID, type + "_ores");
                ore.blockId.forEach(id -> {
                    planItemTag(rlToBlock(tagRl), id, true);
                    planBlockTag(rlToItem(tagRl), id);
                });
            }
        }

        DATA_CACHE.tags().queue(tierHolder);

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();
        Services.CREATIVE_TAB_LOADER.get().registerCreativeTab();

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
                processData(stoneMap, oreMap, mod.providedStones, mod.providedOres);
            }
        }
        List<DataProvider> providers = Services.COMPAT.getListeners(DataProvider.class);
        for (DataProvider provider : providers) {
            List<BaseStone> stones = new ArrayList<>();
            List<BaseOre> ores = new ArrayList<>();
            provider.provideOres(ore -> ores.add(ore.getBase()), stone -> stones.add(stone.getBase()));
            processData(stoneMap, oreMap, stones, ores);
        }
        for (String id : oreMap.keySet()) {
            List<BaseOre> oreList = oreMap.get(id);
            List<String> stones = new ArrayList<>();
            for (BaseOre ore : oreList) {
                stones.addAll(ore.stone);
            }
            Pair<BaseOre, HashSet<BaseStone>> pair = new Pair<>(oreList.get(0).clone(), new HashSet<>());
            if (oreList.size() > 1) {
                pair.getFirst().blockId = new ArrayList<>();
                pair.getFirst().oreName = new ArrayList<>();
                pair.getFirst().stone = new ArrayList<>();
                pair.getFirst().types = new ArrayList<>();
                for (BaseOre baseOre : oreList) {
                    pair.getFirst().blockId.addAll(baseOre.blockId);
                    pair.getFirst().oreName.addAll(baseOre.oreName);
                    pair.getFirst().stone.addAll(baseOre.stone);
                    pair.getFirst().types.addAll(baseOre.types);
                }
                List<String> types = new HashSet<>(pair.getFirst().types).stream().toList();
                pair.getFirst().types.clear();
                pair.getFirst().types.addAll(types);
                List<String> oreNames = new HashSet<>(pair.getFirst().oreName).stream().toList();
                pair.getFirst().oreName.clear();
                pair.getFirst().oreName.addAll(oreNames);
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

        deprecatedApiStandIn();

        List<Pair<Ore, Set<Stone>>> apiListBuilder = new ArrayList<>();
        for (Pair<BaseOre, HashSet<BaseStone>> p : oreStoneList) {
            apiListBuilder.add(new Pair<>(new Ore(p.getFirst()), p.getSecond().stream().map(Stone::new).collect(Collectors.toSet())));
        }
        var apiList = Collections.unmodifiableList(apiListBuilder);
        Services.COMPAT.getListeners(DataReceiver.class).forEach(r -> r.receiveData(apiList));

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
            }
        }
        knownStones.addAll(stoneMap.values());
        stones = knownStones.stream().collect(Collectors.toMap(s -> s.id, Functions.identity()));
        ores = knownOres.stream().collect(Collectors.toMap(o -> o.id, Functions.identity()));
        allPairs = out.stream().flatMap(p -> p.getSecond().stream().map(o -> new Pair<>(p.getFirst(), o))).collect(Collectors.toSet());
        oreStoneList = out;
    }

    @SuppressWarnings("removal")
    private static void deprecatedApiStandIn() {
        for (IOreListModifier listListener : Services.COMPAT.getListeners(IOreListModifier.class)) {
            listListener.modify(new ArrayList<>(), Set.of());
        }
    }

    private static void processData(Map<String, BaseStone> stoneMap, Map<String, List<BaseOre>> oreMap, List<BaseStone> providedStones, List<BaseOre> providedOres) {
        for (BaseStone stone : providedStones) {
            if (!stoneMap.containsKey(stone.id)) stoneMap.put(stone.id, stone);
            else {
                BaseStone stoneOld = stoneMap.get(stone.id);
                List<String> types = new ArrayList<>(stoneOld.types);
                types.addAll(stone.types.stream().filter(s -> !stoneOld.types.contains(s)).toList());
                stoneOld.types = types;
            }
        }
        for (BaseOre ore : providedOres) {
            oreMap.computeIfAbsent(ore.id, k -> new ArrayList<>());
            oreMap.get(ore.id).add(ore);
        }
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
            Supplier<Item> i = itemRegistrar.apply(rlToReg, () -> new BlockItem(b, new Item.Properties()));
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
            knownOres.forEach(ore -> cache.oreMappings.put(ore.id, Set.copyOf(ore.blockId)));
            knownStones.forEach(stone -> cache.stoneMappings.put(stone.id, stone.blockId));

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

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block, boolean inLang) {
        DATA_CACHE.tags().queue(tag, block);
        if (inLang) {
            planTagLang(tag);
        }
    }

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block) {
        planBlockTag(tag, block, false);
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item, boolean inLang) {
        DATA_CACHE.tags().queue(tag, item);
        if (inLang) {
            planTagLang(tag);
        }
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item) {
        planItemTag(tag, item, false);
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
