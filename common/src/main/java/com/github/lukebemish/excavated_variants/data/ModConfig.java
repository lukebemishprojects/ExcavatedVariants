package com.github.lukebemish.excavated_variants.data;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static final Path CONFIG_PATH = Platform.getConfigFolder();
    public static final String FULL_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+".json";
    public static final String DIR_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+"/";
    public static final int CURRENT_VERSION = 3;

    @Expose
    public int format = 0;
    @Expose
    public List<String> priority = new ArrayList<>();
    public List<ModData> mods = new ArrayList<>();
    @Expose
    public List<String> blacklist_ids = new ArrayList<>();
    @Expose
    public List<String> blacklist_stones = new ArrayList<>();
    @Expose
    public List<String> blacklist_ores = new ArrayList<>();
    @Expose
    public boolean attempt_ore_generation_insertion = true;
    @Expose
    public boolean attempt_ore_replacement = true;
    @Expose
    public boolean add_conversion_recipes = true;

    private static final String[] UNEARTHED_STONE_LIST = new String[] {"beige_limestone","conglomerate","gabbro","granodiorite",
            "grey_limestone","limestone","mudstone","phyllite","rhyolite","siltstone","slate","white_granite"};
    private static final String[] UNEARTHED_STONE_EN_LIST = new String[] {"Beige Limestone","Conglomerate","Gabbro","Granodiorite",
            "Grey Limestone","Limestone","Mudstone","Phyllite","Rhyolite","Siltstone","Slate","White Granite"};

    private static List<String> genUnearthedBlockIdList(String oreName, String... toAdd) {
        ArrayList<String> output = new ArrayList<>();

        for (String s : UNEARTHED_STONE_LIST) {
            output.add("unearthed:"+s+"_"+oreName);
        }

        output.addAll(Arrays.asList(toAdd));
        return output;
    }

    private static List<String> genUnearthedStoneIdList(String... toAdd) {
        ArrayList<String> output = new ArrayList<>();

        for (String s : UNEARTHED_STONE_LIST) {
            output.add("unearthed_"+s);
        }

        output.addAll(Arrays.asList(toAdd));
        return output;
    }

    private static List<BaseStone> genUnearthedStoneList(BaseStone... toAdd) {
        ArrayList<BaseStone> output = new ArrayList<>(Arrays.asList(toAdd));

        for (int i = 0; i < UNEARTHED_STONE_LIST.length; i++) {
            String s = UNEARTHED_STONE_LIST[i];
                String enName = UNEARTHED_STONE_EN_LIST[i];
                BaseStone stone = new BaseStone("unearthed_" + s, "unearthed:textures/block/" + s + ".png", enName, "unearthed:"+s,List.of("stone"));
                output.add(stone);
        }

        output.addAll(Arrays.asList(toAdd));

        return output;
    }

    public static ModConfig getDefault() {
        ModConfig output = new ModConfig();
        output.format = CURRENT_VERSION;
        try {
            var url = ModConfig.class.getResource("/default_configs/");
            if (url==null) throw new IOException("default_configs resource not found!");
            Path path;
            try {
                path = Paths.get(url.toURI());
            } catch (FileSystemNotFoundException e) {
                // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
                var env = new HashMap<String, String>();
                path = FileSystems.newFileSystem(url.toURI(), env).getPath("/default_configs/");
            }
            Files.list(path).forEach(p -> {
                try {
                    ModData data = GSON.fromJson(Files.newBufferedReader(p, StandardCharsets.UTF_8), ModData.class);
                    output.mods.add(data);
                } catch (IOException e) {
                    ExcavatedVariants.LOGGER.error("Could not find default config: " + p.getFileName());
                } catch (JsonSyntaxException e) {
                    ExcavatedVariants.LOGGER.error("Issue reading default config: "+p.getFileName());
                }
            });
        } catch (URISyntaxException | IOException e) {
            ExcavatedVariants.LOGGER.error("Could not load some default configs.", e);
        }

        for (ModData m : output.mods) {
            for (BaseOre o : m.provided_ores) {
                o.setupBlockId();
            }
            for (BaseStone s : m.provided_stones) {
                s.setupBlockId();
            }
        }
        output.priority = List.of("minecraft.json");
        return output;
    }

    private static ModConfig load() {
        ModConfig config = new ModConfig();
        try {
            checkExistence();
            config = GSON.fromJson(new FileReader(FULL_PATH), ModConfig.class);
            if (config.format != CURRENT_VERSION) {
                ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
                return getDefault();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            try {
                JsonObject reader = GSON.fromJson(new FileReader(FULL_PATH), JsonObject.class);
                if (reader.has("format") && reader.get("format").isJsonPrimitive() && reader.get("format").getAsJsonPrimitive().isNumber()) {
                    JsonPrimitive p = reader.get("format").getAsJsonPrimitive();
                    try {
                        int i = p.getAsInt();
                        if (i != CURRENT_VERSION) {
                            ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
                            return getDefault();
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                ExcavatedVariants.LOGGER.error("Config existence is inconsistent! Using default config instead...");
                return getDefault();
            }
            ExcavatedVariants.LOGGER.error("Config is not the expected syntax! (It may be outdated). An attempt to load with this config would crash. Using default config instead...");
            return getDefault();
        }
        List<String> loaded = new ArrayList<>();
        for (String p : config.priority) {
            try {
                ModData data = GSON.fromJson(new FileReader(DIR_PATH + p), ModData.class);
                config.mods.add(data);
                loaded.add(p);
            } catch (FileNotFoundException e) {
                ExcavatedVariants.LOGGER.error("Could not find referenced config: "+p);
            } catch (JsonSyntaxException e) {
                ExcavatedVariants.LOGGER.error("Issue reading config: "+p);
            }
        }
        try {
            for (Path s : Files.list(Path.of(DIR_PATH)).toList()) {
                String p = s.getFileName().toString();
                if (!loaded.contains(p)) {
                    try {
                        ModData data = GSON.fromJson(new FileReader(DIR_PATH + p), ModData.class);
                        config.mods.add(data);
                    } catch (FileNotFoundException e) {
                        ExcavatedVariants.LOGGER.error("Could not find config: " + p + ".json");
                    } catch (JsonSyntaxException e) {
                        ExcavatedVariants.LOGGER.error("Issue reading config: "+p+".json");
                    }
                }
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Could not load config directory...", e);
        }
        for (ModData m : config.mods) {
            for (BaseOre o : m.provided_ores) {
                o.setupBlockId();
            }
            for (BaseStone s : m.provided_stones) {
                s.setupBlockId();
            }
        }
        return config;
    }

    public static ModConfig get() {
        ModConfig config = load();
        if (config.format != CURRENT_VERSION) {
            ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
            return getDefault();
        }
        return config;
    }

    public static void save(ModConfig config) {
        try {
            checkExistence();
            FileWriter writer = new FileWriter(FULL_PATH);
            GSON.toJson(config, writer);
            writer.flush();
            writer.close();
            for (ModData m : config.mods) {
                FileWriter w = new FileWriter(DIR_PATH+m.mod_id +".json");
                GSON.toJson(m, w);
                w.flush();
                w.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkExistence() throws IOException {
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID))) Files.createDirectories(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID));
        if (!Files.exists(Paths.get(FULL_PATH))) {
            Files.createFile(Paths.get(FULL_PATH));
            save(getDefault());
        }
    }
}
