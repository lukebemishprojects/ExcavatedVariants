/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.sources.TagSupplier;
import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.BlockPropsModifier;
import dev.lukebemish.excavatedvariants.api.data.modifier.Flag;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import dev.lukebemish.excavatedvariants.impl.client.ClientServices;
import dev.lukebemish.excavatedvariants.impl.data.ModConfig;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ExcavatedVariants {
    public static final int DEFAULT_COMPAT_PRIORITY = -10;

    private ExcavatedVariants() {}
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().setLenient().create();
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static final String MOD_ID = "excavated_variants";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final List<Supplier<Item>> ITEMS = new ArrayList<>();
    private static ModConfig CONFIG;
    public static final DataResourceCache DATA_CACHE = ResourceCache.register(new DataResourceCache(new ResourceLocation(MOD_ID, "data")));
    private static Map<Ore, List<Stone>> NEW_VARIANTS_MAP;
    private static final List<VariantFuture> NEW_VARIANTS = new ArrayList<>();
    public static final Map<ResourceKey<Block>, List<VariantFuture>> NEEDED_KEYS = new IdentityHashMap<>();
    public static final Map<VariantFuture, List<ResourceKey<Block>>> REVERSE_NEEDED_KEYS = new IdentityHashMap<>();
    public static final Deque<VariantFuture> READY_QUEUE = new ArrayDeque<>();
    public static final Map<VariantFuture, ModifiedOreBlock> BLOCKS = new IdentityHashMap<>();
    public static final List<VariantFuture> COMPLETE_VARIANTS = new ArrayList<>();
    public static final RecipePlanner RECIPE_PLANNER = new RecipePlanner();
    public static MappingsCache MAPPINGS_CACHE;

    public synchronized static void setupMap() {
        if (NEW_VARIANTS_MAP == null) {
            Map<Ore, List<Stone>> newVariants = new HashMap<>();
            Map<Ore, Set<Stone>> newVariantsSet = new HashMap<>();
            Map<GroundType, List<Ore>> groundTypeOreMap = new HashMap<>();

            for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
                if (ore.getBlocks().isEmpty()) continue;
                for (ResourceKey<GroundType> groundTypeKey : ore.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey.location() + " does not exist, but is referenced by ore " + ore.getHolder().unwrapKey().orElseThrow().location());
                    }
                    groundTypeOreMap.computeIfAbsent(groundType, k -> new ArrayList<>()).add(ore);
                }
            }

            for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
                for (ResourceKey<GroundType> groundTypeKey : stone.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey.location() + " does not exist, but is referenced by stone " + stone.getHolder().unwrapKey().orElseThrow().location());
                    }
                    var ores = groundTypeOreMap.getOrDefault(groundType, List.of());
                    ore_loop:
                    for (Ore ore : ores) {
                        if (ore.getBlocks().isEmpty()) continue;
                        for (Modifier modifier : RegistriesImpl.MODIFIER_REGISTRY) {
                            if (modifier.variantFilter.matches(ore, stone) && modifier.disable) {
                                continue ore_loop;
                            }
                        }
                        if (!ore.getOriginalStoneBlocks().containsKey(stone.getKeyOrThrow())) {
                            var set = newVariantsSet.computeIfAbsent(ore, k -> new HashSet<>());
                            if (set.add(stone)) {
                                newVariants.computeIfAbsent(ore, k -> new ArrayList<>()).add(stone);
                            }
                        }
                    }
                }
            }

            NEW_VARIANTS_MAP = newVariants;
        }
    }

    public static void init() {
        if (ModLifecycle.getLifecyclePhase() != ModLifecycle.PRE) {
            return;
        }

        RegistriesImpl.bootstrap();

        setupMap();

        for (Map.Entry<Ore, List<Stone>> entry : NEW_VARIANTS_MAP.entrySet()) {
            Ore ore = entry.getKey();
            List<Stone> stones = entry.getValue();
            for (Stone stone : stones) {
                String fullId = computeFullId(ore, stone);
                VariantFuture future = new VariantFuture(fullId, ore, stone);
                ore.addPossibleVariant(stone, new ResourceLocation(ExcavatedVariants.MOD_ID, fullId));
                List<ResourceKey<Block>> keys = new ArrayList<>();
                for (Map.Entry<ResourceKey<Block>, ResourceKey<Stone>> blockEntry : ore.getBlocks().entrySet()) {
                    ResourceKey<Block> key = blockEntry.getKey();
                    Stone originalStone = RegistriesImpl.STONE_REGISTRY.get(blockEntry.getValue());
                    if (originalStone == null) {
                        throw new IllegalStateException("Nonexistent stone "+blockEntry.getValue().location()+" referenced by ore "+ore.getKeyOrThrow().location());
                    }
                    keys.add(key);
                    NEEDED_KEYS.computeIfAbsent(key, k -> new ArrayList<>()).add(future);
                }
                keys.add(stone.block);
                NEEDED_KEYS.computeIfAbsent(stone.block, k -> new ArrayList<>()).add(future);
                REVERSE_NEEDED_KEYS.put(future, keys);
                NEW_VARIANTS.add(future);
            }
        }

        for (Modifier modifier : RegistriesImpl.MODIFIER_REGISTRY) {
            List<VariantFuture> futures = NEW_VARIANTS.stream().filter(p -> modifier.variantFilter.matches(p.ore, p.stone)).toList();
            for (VariantFuture future : futures) {
                future.flags.addAll(modifier.flags);
                if (modifier.properties != null) {
                    future.propsModifiers.add(modifier.properties);
                }
            }
        }

        ExcavatedVariants.DATA_CACHE.planSource(RECIPE_PLANNER);

        ExcavatedVariants.DATA_CACHE.tags().queue(new TagSupplier() {
            @Override
            public Map<ResourceLocation, Set<ResourceLocation>> apply(ResourceGenerationContext context) {
                Map<ResourceLocation, Set<ResourceLocation>> map = TAG_QUEUE.apply(context);
                Map<ResourceLocation, Set<ResourceLocation>> output = new HashMap<>();
                for (var entry : map.entrySet()) {
                    for (var rl : entry.getValue()) {
                        if (entry.getKey().getPath().startsWith("blocks/")) {
                            if (!BuiltInRegistries.BLOCK.containsKey(rl)) {
                                continue;
                            }
                        } else if (entry.getKey().getPath().startsWith("items/")) {
                            if (!BuiltInRegistries.ITEM.containsKey(rl)) {
                                continue;
                            }
                        }
                        output.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(rl);
                    }
                }
                return output;
            }

            // Not worth caching - may change this later depending?

            @Override
            public void reset(ResourceGenerationContext context) {
                TAG_QUEUE.reset(context);
            }
        });

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();
        Services.CREATIVE_TAB_LOADER.get().registerCreativeTab();

        MAPPINGS_CACHE = MappingsCache.load();

        ModLifecycle.setLifecyclePhase(ModLifecycle.REGISTRATION);
    }

    public static synchronized void initPostRegister() {
        if (ModLifecycle.getLifecyclePhase() != ModLifecycle.REGISTRATION) {
            return;
        }

        for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
            ore.bakeExistingBlocks();
        }

        // No reason to keep any of this around; this way some of it can be GCed...
        NEW_VARIANTS.clear();
        NEW_VARIANTS_MAP.clear();
        NEEDED_KEYS.clear();
        REVERSE_NEEDED_KEYS.clear();
        READY_QUEUE.clear();

        for (Modifier modifier : RegistriesImpl.MODIFIER_REGISTRY) {
            if (!modifier.tags.isEmpty()) {
                for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
                    for (var entry : ore.getBlocks().entrySet()) {
                        var stone = Objects.requireNonNull(RegistriesImpl.STONE_REGISTRY.get(entry.getValue()));
                        if (modifier.variantFilter.matches(ore, stone)) {
                            for (ResourceLocation tag : modifier.tags) {
                                if (tag.getPath().startsWith("blocks/"))
                                    planBlockTag(tag.withPath(tag.getPath().substring(7)), entry.getKey().location());
                                else if (tag.getPath().startsWith("items/"))
                                    planItemTag(tag.withPath(tag.getPath().substring(6)), entry.getKey().location());
                            }
                        }
                    }
                }
            }
        }

        for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
            if (BuiltInRegistries.BLOCK.containsKey(stone.block)) {
                for (ResourceKey<GroundType> type : stone.types) {
                    planCombinedTag(Objects.requireNonNull(RegistriesImpl.GROUND_TYPE_REGISTRY.get(type), "Nonexistent ground type " + type.location()).getStoneTagKey().location(), stone.block.location());
                }
            }
        }

        for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
            for (var entry : ore.getBlocks().entrySet()) {
                var block = entry.getKey();
                if (BuiltInRegistries.BLOCK.containsKey(block)) {
                    Stone stone = Objects.requireNonNull(RegistriesImpl.STONE_REGISTRY.get(entry.getValue()), "Nonexistent stone " + ore.getBlocks().get(block).location());
                    for (ResourceKey<GroundType> type : ore.types) {
                        if (!stone.types.contains(type)) continue;
                        planCombinedTag(Objects.requireNonNull(RegistriesImpl.GROUND_TYPE_REGISTRY.get(type), "Nonexistent ground type " + type.location()).getOreTagKey().location(), block.location());
                    }
                    planCombinedTag(ore.getTagKey().location(), block.location());
                    planCombinedTag(
                            stone.getOreTagKey().location(),
                            block.location()
                    );
                }
            }
        }

        if (getConfig().addConversionRecipes) {
            for (VariantFuture future : COMPLETE_VARIANTS) {
                planItemTag(future.ore.getConvertibleTagKey().location(), new ResourceLocation(ExcavatedVariants.MOD_ID, future.fullId));
                RECIPE_PLANNER.oreToBaseOreMap.put(future.ore.getConvertibleTagKey(), future.foundOreKey);
            }
        }

        MiningLevelTagHolder tierHolder = new MiningLevelTagHolder();

        for (VariantFuture future : COMPLETE_VARIANTS) {
            tierHolder.add(future.fullId, future.ore, future.stone);
            var id = new ResourceLocation(ExcavatedVariants.MOD_ID, future.fullId);

            for (ResourceLocation tag : future.ore.tags) {
                planCombinedTag(tag, id);
            }

            for (ResourceLocation tag : future.stone.oreTags) {
                planCombinedTag(tag, id);
            }
        }

        TAG_QUEUE.queue(tierHolder);

        MAPPINGS_CACHE.update();
        MAPPINGS_CACHE.save();

        ModLifecycle.setLifecyclePhase(ModLifecycle.POST);
    }

    public static String computeFullId(ResourceLocation ore, ResourceLocation stone) {
        return ore.getNamespace() + "__" +
                String.join("_", ore.getPath().split("/")) + "__" + stone.getNamespace() + "__" +
                String.join("_",stone.getPath().split("/"));
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

    private static void planBlockTag(ResourceLocation tag, ResourceLocation block) {
        TAG_QUEUE.queue(rlToBlock(tag), block);
    }

    private static void planCombinedTag(ResourceLocation tag, ResourceLocation entry) {
        planBlockTag(tag, entry);
        planItemTag(tag, entry);
    }

    private static void planItemTag(ResourceLocation tag, ResourceLocation item) {
        TAG_QUEUE.queue(rlToItem(tag), item);
    }

    private static ResourceLocation rlToBlock(ResourceLocation rl) {
        return rl.withPrefix("blocks/");
    }

    private static ResourceLocation rlToItem(ResourceLocation rl) {
        return rl.withPrefix("items/");
    }

    public static ModConfig getConfig() {
        if (CONFIG == null) {
            CONFIG = ModConfig.load();
        }
        return CONFIG;
    }

    public static void registerBlockAndItem(BiConsumer<ResourceLocation, Block> blockRegistrar, BiFunction<ResourceLocation, Supplier<Item>, Supplier<Item>> itemRegistrar, VariantFuture future) {
        if (!future.done) {
            future.done = true;
            ResourceLocation rlToReg = new ResourceLocation(ExcavatedVariants.MOD_ID, future.fullId);
            ModifiedOreBlock.setupStaticWrapper(future);
            ModifiedOreBlock b = Services.MAIN_PLATFORM_TARGET.get().makeDefaultOreBlock(future);
            blockRegistrar.accept(rlToReg, b);
            Supplier<Item> i = itemRegistrar.apply(rlToReg, () -> new BlockItem(b, new Item.Properties()));
            ITEMS.add(i);
            BLOCKS.put(future, b);

            if (Services.PLATFORM.isClient()) {
                ClientServices.RENDER_TYPE_HANDLER.setRenderTypeMipped(b);
            }

            COMPLETE_VARIANTS.add(future);
            if (Services.PLATFORM.isClient()) {
                ExcavatedVariantsClient.setUp(future);
            }
        }
    }

    public static class VariantFuture {
        public final Ore ore;
        public final Stone stone;
        public final String fullId;
        public boolean done = false;
        public Block foundStone = null;
        public Block foundOre = null;
        public ResourceKey<Block> foundOreKey = null;
        public Stone foundSourceStone = null;
        public final Set<Flag> flags = new HashSet<>();
        public final List<BlockPropsModifier> propsModifiers = new ArrayList<>();

        public VariantFuture(String s, Ore ore, Stone stone) {
            this.fullId = s;
            this.ore = ore;
            this.stone = stone;
        }
    }
}
