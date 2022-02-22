package excavated_variants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.block.BlockProperties;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dynamic_asset_generator.api.DynAssetGeneratorServerAPI;
import excavated_variants.data.BaseOre;
import excavated_variants.data.BaseStone;
import excavated_variants.data.ModConfig;
import excavated_variants.data.ModData;
import excavated_variants.recipe.OreConversionRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
        for (String id : oreMap.keySet()) {
            List<BaseOre> oreList = oreMap.get(id);
            List<String> stones = new ArrayList<>();
            for (BaseOre ore : oreList) {
                stones.addAll(ore.stone);
            }
            TagBuilder oreDictBuilder = new TagBuilder();
            for (BaseStone stone : stoneMap.values()) {
                if (!stones.contains(stone.id) && oreList.stream().anyMatch(x->x.types.stream().anyMatch(stone.type::contains))) {
                    String full_id = stone.id+"_"+id;
                    if (!ExcavatedVariants.getConfig().blacklist_ids.contains(full_id)) {
                        BLOCKS.register(full_id, () -> makeDefaultOreBlock(full_id, oreList.get(0)));
                        ITEMS.register(full_id, () -> new BlockItem(blocks.get(full_id), new Item.Properties().tab(CreativeTabLoader.EXCAVATED_VARIANTS_TAB)));
                        if (getConfig().add_conversion_recipes) {
                            OreConversionRecipe.oreMap.put(new ResourceLocation(MOD_ID, full_id), oreList.get(0).rl_block_id.get(0));
                        }
                        blockTagBuilder.add(full_id);
                        stoneTag.add(full_id, oreList.get(0));
                        ironTag.add(full_id, oreList.get(0));
                        diamondTag.add(full_id, oreList.get(0));
                        oreDictBuilder.add(full_id);
                    }
                }
            }
            if (Platform.isFabric()) {
                DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("c", "tags/items/" + id + "s.json"),
                        oreDictBuilder.build());
                DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("c", "tags/blocks/" + id + "s.json"),
                        oreDictBuilder.build());
            } else {
                if (id.endsWith("_ore")) {
                    String oreTypeName = id.substring(0, id.length() - 4);
                    DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("forge", "tags/items/ores/" + oreTypeName + ".json"),
                            oreDictBuilder.build());
                    DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("forge", "tags/blocks/ores/" + oreTypeName + ".json"),
                            oreDictBuilder.build());
                }
            }
            if (Arrays.asList("iron_ore","gold_ore","coal_ore","emerald_ore","diamond_ore","redstone_ore","quartz_ore","copper_ore").contains(id)) {
                DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/items/" + id + "s.json"),
                        oreDictBuilder.build());
                DynAssetGeneratorServerAPI.planLoadingStream(new ResourceLocation("minecraft", "tags/blocks/" + id + "s.json"),
                        oreDictBuilder.build());
            }
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
                    pair.first().rl_block_id = new ArrayList<>();
                    pair.first().block_id = new ArrayList<>();
                    pair.first().stone = new ArrayList<>();
                    pair.first().types = new ArrayList<>();
                    for (BaseOre baseOre : oreList) {
                        pair.first().rl_block_id.addAll(baseOre.rl_block_id);
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
                    if (!stones.contains(stone.id) && oreList.stream().anyMatch(x->x.types.stream().anyMatch(stone.type::contains))) {
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

    private static final Map<String, Block> blocks = new HashMap<>();

    public static Block makeDefaultOreBlock(String id, BaseOre ore) {
        Block block = new ModifiedOreBlock(BlockProperties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f), ore);
        blocks.put(id, block);
        return block;
    }
}
