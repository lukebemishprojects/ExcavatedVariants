package com.github.lukebemish.excavated_variants.data;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().excludeFieldsWithoutExposeAnnotation().create();
    public static final Path CONFIG_PATH = Platform.getConfigFolder();
    public static final String FULL_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+".json";
    public static final String DIR_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+"/";
    public static final int CURRENT_VERSION = 4;

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
                    JsonObject json = GSON.fromJson(Files.newBufferedReader(p,StandardCharsets.UTF_8),JsonObject.class);
                    ModData data = ModData.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow(false,(e)-> {
                        ExcavatedVariants.LOGGER.error("Config file is corrupted: {}",e);
                        throw new JsonSyntaxException("");
                    });
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
                JsonObject json = GSON.fromJson(new FileReader(DIR_PATH+p),JsonObject.class);
                ModData data = ModData.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow(false,(e)-> {
                    ExcavatedVariants.LOGGER.error("Config file is corrupted: {}",e);
                    throw new JsonSyntaxException("");
                });
                config.mods.add(data);
                loaded.add(p+".json");
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
                        JsonObject json = GSON.fromJson(new FileReader(DIR_PATH+p),JsonObject.class);
                        ModData data = ModData.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow(false,(e)-> {
                            ExcavatedVariants.LOGGER.error("Config file is corrupted: {}",e);
                            throw new JsonSyntaxException("");
                        });
                        config.mods.add(data);
                    } catch (FileNotFoundException e) {
                        ExcavatedVariants.LOGGER.error("Could not find config: " + p);
                    } catch (JsonSyntaxException e) {
                        ExcavatedVariants.LOGGER.error("Issue reading config: "+p);
                    }
                }
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Could not load config directory...", e);
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
                JsonElement json = ModData.CODEC.encodeStart(JsonOps.INSTANCE,m).getOrThrow(false,(e)-> {
                    ExcavatedVariants.LOGGER.error("Default config file is corrupted during write. Something is very wrong: {}",e);
                });
                if (json != null) {
                    FileWriter w = new FileWriter(DIR_PATH+m.mod_id +".json");
                    GSON.toJson(json, w);
                    w.flush();
                    w.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkExistence() throws IOException {
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID))) Files.createDirectories(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID));
        Path path = Path.of(FULL_PATH);
        if (!Files.exists(path)) {
            Files.createFile(path);
            save(getDefault());
        }
    }
}
