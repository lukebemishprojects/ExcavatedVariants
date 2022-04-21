package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.client.RenderTypeHandler;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.util.Pair;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import com.github.lukebemish.dynamic_asset_generator.api.DynAssetGeneratorServerAPI;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.data.ModConfig;
import com.github.lukebemish.excavated_variants.data.ModData;
import com.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExcavatedVariants {
    public static final String MOD_ID = "excavated_variants";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MOD_ID, Registry.RECIPE_SERIALIZER_REGISTRY);

    public static final RegistrySupplier<RecipeSerializer<OreConversionRecipe>> ORE_CONVERSION = RECIPE_SERIALIZERS.register("ore_conversion",()->
            new SimpleRecipeSerializer<>(OreConversionRecipe::new));

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static List<Pair<BaseOre, List<BaseStone>>> oreStoneList;

    public static void init() {
        setupMap();
        TagBuilder blockTagBuilder = new TagBuilder();
        MiningLevelTagGenerator stoneTag = new MiningLevelTagGenerator("stone");
        MiningLevelTagGenerator ironTag = new MiningLevelTagGenerator("iron");
        MiningLevelTagGenerator diamondTag = new MiningLevelTagGenerator("diamond");
        Collection<String> modids = Platform.getModIds();
        Map<String, BaseStone> stoneMap = new HashMap<>();
        Map<String, List<BaseOre>> oreMap = new HashMap<>();
        for (ModData mod : ExcavatedVariants.getConfig().mods) {
            if (modids.contains(mod.mod_id)) {
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
        Map<ResourceLocation, TagBuilder> builders = new HashMap<>();
        for (String id : oreMap.keySet()) {
            List<BaseOre> oreList = oreMap.get(id);
            Set<String> oreNames = oreList.stream().flatMap(x->x.orename.stream()).collect(Collectors.toSet());
            List<String> stones = new ArrayList<>();
            for (BaseOre ore : oreList) {
                stones.addAll(ore.stone);
            }
            List<String> ids = new ArrayList<>();
            for (BaseStone stone : stoneMap.values()) {
                if (!stones.contains(stone.id) && oreList.stream().anyMatch(x->x.types.stream().anyMatch(stone.types::contains))) {
                    String full_id = stone.id+"_"+id;
                    if (!ExcavatedVariants.getConfig().blacklist_ids.contains(full_id)) {
                        blockList.add(new RegistryFuture(full_id,oreList.get(0), stone));
                        if (getConfig().add_conversion_recipes) {
                            OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, full_id), oreList.get(0).block_id.get(0));
                        }
                        blockTagBuilder.add(full_id);
                        stoneTag.add(full_id, oreList.get(0));
                        ironTag.add(full_id, oreList.get(0));
                        diamondTag.add(full_id, oreList.get(0));
                        ids.add(full_id);
                    }
                }
            }
            for (String orename : oreNames) {
                for (String this_id : ids) {
                    if (Platform.isFabric()) {
                        builders.computeIfAbsent(new ResourceLocation("c", "tags/items/" + orename + "s.json"), k->new TagBuilder()).add(this_id);
                        builders.computeIfAbsent(new ResourceLocation("c", "tags/blocks/" + orename + "s.json"), k->new TagBuilder()).add(this_id);
                    } else {
                        if (orename.endsWith("_ore")) {
                            String oreTypeName = orename.substring(0, orename.length() - 4);
                            builders.computeIfAbsent(new ResourceLocation("forge", "tags/items/ores/" + oreTypeName + ".json"), k->new TagBuilder()).add(this_id);
                            builders.computeIfAbsent(new ResourceLocation("forge", "tags/blocks/ores/" + oreTypeName + ".json"), k->new TagBuilder()).add(this_id);
                        }
                    }
                    if (Arrays.asList("iron_ore", "gold_ore", "coal_ore", "emerald_ore", "diamond_ore", "redstone_ore", "quartz_ore", "copper_ore", "netherite_scrap_ore").contains(orename)) {
                        builders.computeIfAbsent(new ResourceLocation("minecraft", "tags/items/" + orename + "s.json"), k->new TagBuilder()).add(this_id);
                        builders.computeIfAbsent(new ResourceLocation("minecraft", "tags/blocks/" + orename + "s.json"), k->new TagBuilder()).add(this_id);
                    }
                }
            }
        }
        for (ResourceLocation key : builders.keySet()) {
            DynAssetGeneratorServerAPI.planLoadingStream(key,builders.get(key).build());
        }

        DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/blocks/mineable/pickaxe.json"),
                blockTagBuilder.build());
        if (!Platform.isFabric()) {
            DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("forge", "tags/blocks/ores.json"),
                    blockTagBuilder.build());
            DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("forge", "tags/items/ores.json"),
                    blockTagBuilder.build());
        }
        DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/blocks/needs_stone_tool.json"),
                stoneTag);
        DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/blocks/needs_iron_tool.json"),
                ironTag);
        DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/blocks/needs_diamond_tool.json"),
                diamondTag);
        BLOCKS.register();
        ITEMS.register();
        RECIPE_SERIALIZERS.register();
        
        registerFeatures();

        loaded = true;
    }

    public static boolean setupMap() {
        if (oreStoneList == null || oreStoneList.size() == 0) {
            Collection<String> modids;
            try {
                modids = Platform.getModIds();
            } catch (NullPointerException e) {
                oreStoneList = new ArrayList<>();
                return false;
            }
            oreStoneList = new ArrayList<>();
            Map<String, BaseStone> stoneMap = new HashMap<>();
            Map<String, List<BaseOre>> oreMap = new HashMap<>();
            for (ModData mod : ExcavatedVariants.getConfig().mods) {
                if (modids.contains(mod.mod_id)) {
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
                Pair<BaseOre, List<BaseStone>> pair = new Pair<>(oreList.get(0).clone(), new ArrayList<>());
                if (oreList.size() > 1) {
                    pair.first().block_id = new ArrayList<>();
                    pair.first().block_id = new ArrayList<>();
                    pair.first().stone = new ArrayList<>();
                    pair.first().types = new ArrayList<>();
                    for (BaseOre baseOre : oreList) {
                        pair.first().block_id.addAll(baseOre.block_id);
                        pair.first().block_id.addAll(baseOre.block_id);
                        pair.first().stone.addAll(baseOre.stone);
                        pair.first().types.addAll(baseOre.types);
                    }
                    List<String> types = new HashSet<>(pair.first().types).stream().toList();
                    pair.first().types.clear();
                    pair.first().types.addAll(types);
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
        }
        return true;
    }

    private static ModConfig configs;

    public static ModConfig getConfig() {
        if (configs == null) {
            configs = ModConfig.get();
        }
        return configs;
    }

    @ExpectPlatform
    public static void registerFeatures() {
        throw new AssertionError();
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

    public static void registerBlockAndItem(BiConsumer<ResourceLocation,Block> blockRegistrar, BiConsumer<ResourceLocation,Item> itemRegistrar, RegistryFuture future) {
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
            Item i = new BlockItem(b, new Item.Properties().tab(CreativeTabLoader.EXCAVATED_VARIANTS_TAB));
            itemRegistrar.accept(rlToReg, i);
            items.add(()->i);

            RenderTypeHandler.setRenderTypeMipped(b);
        }
    }

    public static List<Supplier<Item>> getItems() {
        return items;
    }

    private static final List<Supplier<Item>> items = new ArrayList<>();
    private static final Map<String, ModifiedOreBlock> blocks = new HashMap<>();
    private static final List<RegistryFuture> blockList = new ArrayList<>();

    @ExpectPlatform
    public static ModifiedOreBlock makeDefaultOreBlock(String id, BaseOre ore, BaseStone stone) {
        throw new AssertionError();
    }

    public static ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED;
    public static PlacedFeature ORE_REPLACER_PLACED;

    private static boolean loaded = false;

    public static boolean hasLoaded() {
        return loaded;
    }

    public static List<ResourceLocation> loadedBlockRLs = new ArrayList<>();
}
