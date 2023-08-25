/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import blue.endless.jankson.Jankson;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.sources.TagSupplier;
import dev.lukebemish.excavatedvariants.impl.client.ClientServices;
import dev.lukebemish.excavatedvariants.impl.data.*;
import dev.lukebemish.excavatedvariants.impl.data.modifier.Modifier;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import dev.lukebemish.excavatedvariants.impl.recipe.OreConversionRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
    private static final List<Supplier<Item>> ITEMS = new ArrayList<>();
    private static boolean LOADED = false;
    private static ModConfig CONFIG;
    public static final DataResourceCache DATA_CACHE = ResourceCache.register(new DataResourceCache(new ResourceLocation(MOD_ID, "data")));
    public static Map<Ore, List<Stone>> NEW_VARIANTS_MAP;
    public static List<Pair<Ore, Stone>> NEW_VARIANTS;
    public static final Map<ResourceKey<Block>, List<RegistryFuture>> NEEDED_KEYS = new IdentityHashMap<>();
    public static final Map<RegistryFuture, List<ResourceKey<Block>>> REVERSE_NEEDED_KEYS = new IdentityHashMap<>();
    public static final Deque<RegistryFuture> READY_QUEUE = new ArrayDeque<>();

    public static final Set<String> VANILLA_ORE_NAMES = Set.of("iron", "gold", "coal", "emerald", "diamond", "redstone", "copper", "lapis");

    public synchronized static void setupMap() {
        if (NEW_VARIANTS_MAP == null) {
            Map<Ore, List<Stone>> newVariants = new HashMap<>();
            List<Pair<Ore, Stone>> newVariantsList = new ArrayList<>();
            Map<Ore, Set<Stone>> newVariantsSet = new HashMap<>();
            Map<GroundType, List<Ore>> groundTypeOreMap = new HashMap<>();

            for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
                for (ResourceKey<GroundType> groundTypeKey : ore.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey + " does not exist, but is referenced by ore " + ore.getHolder());
                    }
                    groundTypeOreMap.computeIfAbsent(groundType, k -> new ArrayList<>()).add(ore);
                }
            }

            for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
                for (ResourceKey<GroundType> groundTypeKey : stone.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey + " does not exist, but is referenced by stone " + stone.getHolder());
                    }
                    var ores = groundTypeOreMap.getOrDefault(groundType, List.of());
                    for (Ore ore : ores) {
                        if (!ore.getBlocks().containsValue(stone.getKeyOrThrow())) {
                            var set = newVariantsSet.computeIfAbsent(ore, k -> new HashSet<>());
                            if (set.add(stone)) {
                                newVariants.computeIfAbsent(ore, k -> new ArrayList<>()).add(stone);
                                newVariantsList.add(new Pair<>(ore, stone));
                            }
                        }
                    }
                }
            }

            // TODO: filter stuff out here

            NEW_VARIANTS_MAP = Map.copyOf(newVariants);
            NEW_VARIANTS = List.copyOf(newVariantsList);
        }
    }

    public static void init() {
        RegistriesImpl.bootstrap();

        setupMap();

        MiningLevelTagHolder tierHolder = new MiningLevelTagHolder();

        for (Map.Entry<Ore, List<Stone>> entry : NEW_VARIANTS_MAP.entrySet()) {
            Ore ore = entry.getKey();
            List<Stone> stones = entry.getValue();
            List<String> ids = new ArrayList<>();
            for (Stone stone : stones) {
                String fullId = computeFullId(ore, stone);
                tierHolder.add(fullId, ore, stone);
                RegistryFuture future = new RegistryFuture(fullId, ore, stone);
                ore.addPossibleVariant(stone, new ResourceLocation(ExcavatedVariants.MOD_ID, fullId));
                List<ResourceKey<Block>> keys = new ArrayList<>();
                for (Map.Entry<ResourceKey<Block>, ResourceKey<Stone>> blockEntry : ore.getBlocks().entrySet()) {
                    if (blockEntry.getValue() != stone.getKeyOrThrow()) {
                        continue;
                    }
                    ResourceKey<Block> key = blockEntry.getKey();
                    Stone originalStone = RegistriesImpl.STONE_REGISTRY.get(blockEntry.getValue());
                    if (originalStone == null) {
                        throw new IllegalStateException("Nonexistent stone "+blockEntry.getValue().location()+" referenced by ore "+ore.getKeyOrThrow().location());
                    }
                    keys.add(key);
                    NEEDED_KEYS.computeIfAbsent(key, k -> new ArrayList<>()).add(future);
                }
                REVERSE_NEEDED_KEYS.put(future, keys);

                OreConversionRecipe.ORE_MAP.put(new ResourceLocation(ExcavatedVariants.MOD_ID, fullId), ore.getBlocks().keySet().stream().sorted().toList());

                for (ResourceKey<GroundType> type : Sets.union(stone.types, ore.types)) {
                    ResourceLocation tagRl = new ResourceLocation(type.location().getNamespace(), "ores/" + type.location().getPath());
                    ResourceLocation id = new ResourceLocation(ExcavatedVariants.MOD_ID, fullId);
                    planCombinedTag(tagRl, id);
                }
                ids.add(fullId);
            }
            for (String oreTypeName : ore.names) {
                String oreName = oreTypeName + "_ores";
                for (String thisId : ids) {
                    if (Services.PLATFORM.isQuilt()) {
                        planCombinedTag(new ResourceLocation("c", "ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, thisId), true);
                        planCombinedTag(new ResourceLocation("c", oreName + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, thisId), true);
                    } else {
                        planCombinedTag(new ResourceLocation("forge", "ores/" + oreTypeName), new ResourceLocation(ExcavatedVariants.MOD_ID, thisId), true);
                    }
                    if (VANILLA_ORE_NAMES.contains(oreName)) {
                        planCombinedTag(new ResourceLocation("minecraft", oreName + "s"), new ResourceLocation(ExcavatedVariants.MOD_ID, thisId));
                    }
                }
            }
        }

        for (Modifier modifier : RegistriesImpl.MODIFIER_REGISTRY) {
            if (!modifier.tags.isEmpty()) {
                List<ResourceLocation> locations = NEW_VARIANTS.stream().filter(p -> modifier.variantFilter.matches(p.getFirst(), p.getSecond()))
                        .map(p -> new ResourceLocation(ExcavatedVariants.MOD_ID, computeFullId(p.getFirst(), p.getSecond()))).toList();
                modifier.tags.forEach(tag -> locations.forEach(rl -> {
                    if (tag.getPath().startsWith("blocks/"))
                        planBlockTag(tag, rl);
                    else if (tag.getPath().startsWith("items/"))
                        planItemTag(tag, rl);
                }));
            }
        }

        for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
            for (ResourceKey<GroundType> type : stone.types) {
                ResourceLocation tagRl = new ResourceLocation(type.location().getNamespace(), type + "stones/"+type.location().getPath());
                planCombinedTag(rlToItem(tagRl), stone.block.location());
            }
        }

        for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
            for (ResourceKey<GroundType> type : ore.types) {
                ResourceLocation tagRl = new ResourceLocation(type.location().getNamespace(), type + "ores/"+type.location().getPath());
                for (ResourceKey<Block> blocks : ore.getBlocks().keySet()) {
                    planCombinedTag(rlToBlock(tagRl), blocks.location());
                }
            }
        }

        TAG_QUEUE.queue(tierHolder);

        ExcavatedVariants.DATA_CACHE.tags().queue(new TagSupplier() {
            @Override
            public Map<ResourceLocation, Set<ResourceLocation>> apply(ResourceGenerationContext context) {
                Map<ResourceLocation, Set<ResourceLocation>> map = TAG_QUEUE.apply(context);
                Map<ResourceLocation, Set<ResourceLocation>> output = new HashMap<>();
                for (var entry : map.entrySet()) {
                    for (var rl : entry.getValue()) {
                        if (rl.getPath().startsWith("blocks/")) {
                            ResourceLocation remainder = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7));
                            if (!BuiltInRegistries.BLOCK.containsKey(remainder)) {
                                continue;
                            }
                        } else if (rl.getPath().startsWith("items/")) {
                            ResourceLocation remainder = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(6));
                            if (!BuiltInRegistries.ITEM.containsKey(remainder)) {
                                continue;
                            }
                        }
                        output.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(rl);
                    }
                }
                return output;
            }

            @Override
            public @Nullable String createSupplierCacheKey(ResourceLocation outRl, ResourceGenerationContext context) {
                return TAG_QUEUE.createSupplierCacheKey(outRl, context);
            }

            @Override
            public void reset(ResourceGenerationContext context) {
                TAG_QUEUE.reset(context);
            }
        });

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();
        Services.CREATIVE_TAB_LOADER.get().registerCreativeTab();

        setHasLoaded();
    }

    private static synchronized void setHasLoaded() {
        LOADED = true;
    }

    public static boolean hasLoaded() {
        return LOADED;
    }

    public static String computeFullId(ResourceLocation ore, ResourceLocation stone) {
        return ore.getNamespace() + "__" + ore.getPath() + "__" + stone.getNamespace() + "__" + stone.getPath();
    }

    public static String computeFullId(ResourceKey<Ore> ore, ResourceKey<Stone> stone) {
        return computeFullId(ore.location(), stone.location());
    }

    public static String computeFullId(Ore ore, Stone stone) {
        return computeFullId(
                ore.getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ore")),
                stone.getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered stone"))
        );
    }

    public static final TagSupplier.TagBakery TAG_QUEUE = new TagSupplier.TagBakery();

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block, boolean inLang) {
        TAG_QUEUE.queue(rlToBlock(tag), block);
        if (inLang) {
            ExcavatedVariants.planTagLang(rlToBlock(tag));
        }
    }

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block) {
        planBlockTag(tag, block, false);
    }

    private static void planCombinedTag(ResourceLocation tag, ResourceLocation entry, boolean inLang) {
        planBlockTag(tag, entry, inLang);
        planItemTag(tag, entry, inLang);
    }

    private static void planCombinedTag(ResourceLocation tag, ResourceLocation entry) {
        planCombinedTag(tag, entry, false);
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item, boolean inLang) {
        TAG_QUEUE.queue(rlToItem(tag), item);
        if (inLang) {
            ExcavatedVariants.planTagLang(rlToItem(tag));
        }
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item) {
        planItemTag(tag, item, false);
    }

    private static ResourceLocation rlToBlock(ResourceLocation rl) {
        return rl.withPrefix("blocks/");
    }

    private static ResourceLocation rlToItem(ResourceLocation rl) {
        return rl.withPrefix("items/");
    }

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

    public static ModConfig getConfig() {
        if (CONFIG == null) {
            CONFIG = ModConfig.load();
        }
        return CONFIG;
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
            ITEMS.add(i);

            ClientServices.RENDER_TYPE_HANDLER.setRenderTypeMipped(b);
        }
    }

    public static class RegistryFuture {
        public final Ore ore;
        public final Stone stone;
        public final String fullId;
        public boolean done = false;
        public boolean foundStone = false;
        public boolean foundOre = false;

        public RegistryFuture(String s, Ore ore, Stone stone) {
            this.fullId = s;
            this.ore = ore;
            this.stone = stone;
        }
    }
}
