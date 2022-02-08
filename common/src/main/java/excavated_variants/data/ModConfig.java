package excavated_variants.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import excavated_variants.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_PATH = Platform.getConfigFolder();
    public static final String FULL_PATH = CONFIG_PATH.toString() + "/"+ ExcavatedVariants.MOD_ID+".json";

    public List<ModData> mods = new ArrayList<>();
    public List<String> blacklist_ids = new ArrayList<>();
    public List<String> blacklist_stones = new ArrayList<>();
    public List<String> blacklist_ores = new ArrayList<>();
    public boolean attempt_ore_generation_insertion = true;

    public static ModConfig getDefault() {
        ModConfig output = new ModConfig();
        output.mods.add(new ModData("create",
                Arrays.asList(new BaseStone("asurine", new ResourceLocation("create","textures/block/palettes/stone_types/asurine.png"), "Asurine ", new ResourceLocation("create","asurine")),
                        new BaseStone("crimsite", new ResourceLocation("create","textures/block/palettes/stone_types/crimsite.png"), "Crimsite ", new ResourceLocation("create","crimsite")),
                        new BaseStone("limestone", new ResourceLocation("create","textures/block/palettes/stone_types/limestone.png"), "Limestone", new ResourceLocation("create","limestone")),
                        new BaseStone("ochrum", new ResourceLocation("create","textures/block/palettes/stone_types/ochrum.png"), "Ochrum ", new ResourceLocation("create","ochrum")),
                        new BaseStone("scorchia", new ResourceLocation("create","textures/block/palettes/stone_types/scorchia.png"), "Scorchia ", new ResourceLocation("create","scorchia")),
                        new BaseStone("scoria", new ResourceLocation("create","textures/block/palettes/stone_types/scoria.png"), "Scoria ", new ResourceLocation("create","scoria")),
                        new BaseStone("veridium", new ResourceLocation("create","textures/block/palettes/stone_types/veridium.png"), "Veridium ", new ResourceLocation("create","veridium"))),
                Arrays.asList(new BaseOre("zinc_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("create","textures/block/zinc_ore.png"),new ResourceLocation("create","zinc_ore"), "Zinc Ore"))));
        output.mods.add(new ModData("minecraft",
                Arrays.asList(new BaseStone("stone", new ResourceLocation("minecraft","textures/block/stone.png"), "", new ResourceLocation("minecraft","stone")),
                        new BaseStone("deepslate", new ResourceLocation("minecraft","textures/block/deepslate.png"), "Deepslate ", new ResourceLocation("minecraft","deepslate")),
                        new BaseStone("andesite", new ResourceLocation("minecraft","textures/block/andesite.png"), "Andesite ", new ResourceLocation("minecraft","andesite")),
                        new BaseStone("diorite", new ResourceLocation("minecraft","textures/block/diorite.png"), "Diorite ", new ResourceLocation("minecraft","diorite")),
                        new BaseStone("granite", new ResourceLocation("minecraft","textures/block/granite.png"), "Granite ", new ResourceLocation("minecraft","granite")),
                        new BaseStone("sandstone", new ResourceLocation("minecraft","textures/block/sandstone.png"), "Sandstone ", new ResourceLocation("minecraft","sandstone")),
                        new BaseStone("netherrack", new ResourceLocation("minecraft","textures/block/netherrack.png"), "Netherrack ", new ResourceLocation("minecraft","netherrack"))),
                Arrays.asList(new BaseOre("coal_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/coal_ore.png"),new ResourceLocation("minecraft","coal_ore"), "Coal Ore"),
                        new BaseOre("iron_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/iron_ore.png"),new ResourceLocation("minecraft","iron_ore"), "Iron Ore"),
                        new BaseOre("gold_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/gold_ore.png"),new ResourceLocation("minecraft","gold_ore"), "Gold Ore"),
                        new BaseOre("copper_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/copper_ore.png"),new ResourceLocation("minecraft","copper_ore"), "Copper Ore"),
                        new BaseOre("emerald_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/emerald_ore.png"),new ResourceLocation("minecraft","emerald_ore"), "Emerald Ore"),
                        new BaseOre("diamond_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/diamond_ore.png"),new ResourceLocation("minecraft","diamond_ore"), "Diamond Ore"),
                        new BaseOre("quartz_ore",Arrays.asList("netherrack"), new ResourceLocation("minecraft","textures/block/nether_quartz_ore.png"),new ResourceLocation("minecraft","nether_quartz_ore"), "Quartz Ore"),
                        new BaseOre("redstone_ore",Arrays.asList("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/redstone_ore.png"),new ResourceLocation("minecraft","redstone_ore"), "Redstone Ore"))));
        output.blacklist_ores.add("quartz_ore");
        output.blacklist_stones.add("netherrack");
        return output;
    }

    private static ModConfig load() {
        ModConfig config = new ModConfig();
        try {
            checkExistence();
            config = GSON.fromJson(new FileReader(FULL_PATH), ModConfig.class);
            save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static ModConfig get() {
        ModConfig config = load();
        return config;
    }

    public static void save(ModConfig config) {
        try {
            checkExistence();
            FileWriter writer = new FileWriter(FULL_PATH);
            GSON.toJson(config, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkExistence() throws IOException {
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(Paths.get(FULL_PATH))) {
            Files.createFile(Paths.get(FULL_PATH));
            save(getDefault());
        }
    }
}
