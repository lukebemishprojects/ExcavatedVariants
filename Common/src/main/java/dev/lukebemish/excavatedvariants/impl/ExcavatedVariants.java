/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import blue.endless.jankson.Jankson;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
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
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static final Set<String> VANILLA_ORE_NAMES = Set.of("iron", "gold", "coal", "emerald", "diamond", "redstone", "copper", "lapis");

    public static String universalCacheKey(String remainder) {
        if (UNIVERSAL_CACHE_KEY == null || remainder == null) return null;
        return Services.PLATFORM.getModVersion()+":"+UNIVERSAL_CACHE_KEY+"\n"+remainder;
    }

    public static String UNIVERSAL_CACHE_KEY = null;

    public synchronized static void setupMap() {
        if (NEW_VARIANTS_MAP == null) {
            MutableBoolean noCacheKey = new MutableBoolean(false);
            StringBuilder universalCacheKey = new StringBuilder();
            Map<Ore, List<Stone>> newVariants = new HashMap<>();
            Map<Ore, Set<Stone>> newVariantsSet = new HashMap<>();
            Map<GroundType, List<Ore>> groundTypeOreMap = new HashMap<>();

            for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
                universalCacheKey.append(ore.getKeyOrThrow().location());
                universalCacheKey.append('@');
                Ore.CODEC.encodeStart(JsonOps.INSTANCE, ore).map(GSON_CONDENSED::toJson).result().ifPresentOrElse(universalCacheKey::append, noCacheKey::setTrue);
                universalCacheKey.append(',');
                for (ResourceKey<GroundType> groundTypeKey : ore.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey.location() + " does not exist, but is referenced by ore " + ore.getHolder().unwrapKey().orElseThrow().location());
                    }
                    groundTypeOreMap.computeIfAbsent(groundType, k -> new ArrayList<>()).add(ore);
                }
            }

            for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
                universalCacheKey.append(stone.getKeyOrThrow().location());
                universalCacheKey.append('@');
                Stone.CODEC.encodeStart(JsonOps.INSTANCE, stone).map(GSON_CONDENSED::toJson).result().ifPresentOrElse(universalCacheKey::append, noCacheKey::setTrue);
                universalCacheKey.append(',');
                for (ResourceKey<GroundType> groundTypeKey : stone.types) {
                    var groundType = RegistriesImpl.GROUND_TYPE_REGISTRY.get(groundTypeKey);
                    if (groundType == null) {
                        throw new RuntimeException("Ground type " + groundTypeKey.location() + " does not exist, but is referenced by stone " + stone.getHolder().unwrapKey().orElseThrow().location());
                    }
                    var ores = groundTypeOreMap.getOrDefault(groundType, List.of());
                    for (Ore ore : ores) {
                        if (!ore.getBlocks().containsValue(stone.getKeyOrThrow())) {
                            var set = newVariantsSet.computeIfAbsent(ore, k -> new HashSet<>());
                            if (set.add(stone)) {
                                newVariants.computeIfAbsent(ore, k -> new ArrayList<>()).add(stone);
                            }
                        }
                    }
                }
            }

            for (GroundType groundType : RegistriesImpl.GROUND_TYPE_REGISTRY) {
                universalCacheKey.append(groundType.getKeyOrThrow().location());
                universalCacheKey.append('@');
                GroundType.CODEC.encodeStart(JsonOps.INSTANCE, groundType).map(GSON_CONDENSED::toJson).result().ifPresentOrElse(universalCacheKey::append, noCacheKey::setTrue);
                universalCacheKey.append(',');
            }

            for (Modifier modifier : RegistriesImpl.MODIFIER_REGISTRY) {
                universalCacheKey.append(modifier.getKeyOrThrow().location());
                universalCacheKey.append('@');
                for (ResourceLocation rl : modifier.tags) {
                    universalCacheKey.append(rl.toString());
                }
                universalCacheKey.append(',');
            }

            // TODO: filter stuff out here

            NEW_VARIANTS_MAP = newVariants;
            if (noCacheKey.isFalse()) {
                UNIVERSAL_CACHE_KEY = universalCacheKey.toString();
            }
        }
    }

    public static void init() {
        if (ModLifecycle.getState() != ModLifecycle.State.PRE) {
            return;
        }

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

                ResourceLocation id = new ResourceLocation(ExcavatedVariants.MOD_ID, fullId);

                for (ResourceKey<GroundType> type : Sets.union(stone.types, ore.types)) {
                    planCombinedTag(Objects.requireNonNull(RegistriesImpl.GROUND_TYPE_REGISTRY.get(type), "Nonexistent ground type "+type.location()).getOreTagKey().location(), id);
                }

                planCombinedTag(ore.getTagKey().location(), id);

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
            List<VariantFuture> futures = NEW_VARIANTS.stream().filter(p -> modifier.variantFilter.matches(p.ore, p.stone)).toList();
            for (VariantFuture future : futures) {
                future.flags.addAll(modifier.flags);
                future.propsModifiers.add(modifier.properties);
            }
        }

        for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
            for (ResourceKey<GroundType> type : stone.types) {
                planCombinedTag(Objects.requireNonNull(RegistriesImpl.GROUND_TYPE_REGISTRY.get(type), "Nonexistent ground type "+type.location()).getStoneTagKey().location(), stone.block.location());
            }
        }

        for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
            for (ResourceKey<Block> block : ore.getBlocks().keySet()) {
                for (ResourceKey<GroundType> type : ore.types) {
                    planCombinedTag(Objects.requireNonNull(RegistriesImpl.GROUND_TYPE_REGISTRY.get(type), "Nonexistent ground type "+type.location()).getOreTagKey().location(), block.location());
                }
                planCombinedTag(ore.getTagKey().location(), block.location());
                planCombinedTag(
                        Objects.requireNonNull(
                                RegistriesImpl.STONE_REGISTRY.get(ore.getBlocks().get(block)),
                                "Nonexistent stone "+ore.getBlocks().get(block).location()
                        ).getOreTagKey().location(),
                        block.location()
                );
            }
        }

        ExcavatedVariants.DATA_CACHE.planSource(RECIPE_PLANNER);

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

            // Not worth caching - may change this later depending?

            @Override
            public void reset(ResourceGenerationContext context) {
                TAG_QUEUE.reset(context);
            }
        });

        Services.MAIN_PLATFORM_TARGET.get().registerFeatures();
        Services.CREATIVE_TAB_LOADER.get().registerCreativeTab();

        ModLifecycle.inRegistrationState();
    }

    public static synchronized void initPostRegister() {
        if (ModLifecycle.getState() != ModLifecycle.State.REGISTRATION) {
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
                                    planBlockTag(tag, entry.getKey().location());
                                else if (tag.getPath().startsWith("items/"))
                                    planItemTag(tag, entry.getKey().location());
                            }
                        }
                    }
                }
            }
        }

        if (getConfig().addConversionRecipes) {
            for (VariantFuture future : COMPLETE_VARIANTS) {
                planItemTag(future.ore.getConvertibleTagKey().location(), new ResourceLocation(ExcavatedVariants.MOD_ID, future.fullId));
                RECIPE_PLANNER.oreToBaseOreMap.put(future.ore.getConvertibleTagKey(), future.foundOreKey);
            }
        }

        ModLifecycle.inPostState();
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
