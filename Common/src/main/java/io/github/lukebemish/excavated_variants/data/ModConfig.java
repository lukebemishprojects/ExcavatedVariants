package io.github.lukebemish.excavated_variants.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().excludeFieldsWithoutExposeAnnotation().create();
    public static final Path CONFIG_PATH = Services.PLATFORM.getConfigFolder();
    public static final String FULL_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+".json";

    public static final Codec<ModConfig> CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.BOOL.fieldOf("attempt_ore_gen_insertion").forGetter(c->c.attempt_ore_gen_insertion),
            Codec.BOOL.fieldOf("attempt_worldgen_replacement").forGetter(c->c.attempt_worldgen_replacement),
            Codec.BOOL.fieldOf("add_conversion_recipes").forGetter(c->c.add_conversion_recipes),
            Codec.BOOL.fieldOf("jei_rei_compat").forGetter(c->c.jei_rei_compat),
            Codec.BOOL.fieldOf("unobtainable_variants").forGetter(c->c.unobtainable_variants)
    ).apply(instance,ModConfig::new));

    public final boolean attempt_ore_gen_insertion;
    public final boolean attempt_worldgen_replacement;
    public final boolean add_conversion_recipes;
    public final boolean jei_rei_compat;
    public final boolean unobtainable_variants;

    public final ConfigResource configResource = ConfigResource.empty();

    public final List<ModData> mods = new ArrayList<>();

    private ModConfig(boolean attempt_ore_gen_insertion, boolean attempt_worldgen_replacement, boolean add_conversion_recipes, boolean jei_rei_compat,
                     boolean unobtainable_variants) {
        this.attempt_ore_gen_insertion = attempt_ore_gen_insertion;
        this.attempt_worldgen_replacement = attempt_worldgen_replacement;
        this.add_conversion_recipes = add_conversion_recipes;
        this.jei_rei_compat = jei_rei_compat;
        this.unobtainable_variants = unobtainable_variants;
    }

    private static ModConfig defaultConfig() {
        return new ModConfig(true, true, true, true, false);
    }

    public static ModConfig load() {
        try {
            checkExistenceOrSave();
            Path path = Path.of(FULL_PATH);
            JsonObject json = GSON.fromJson(new FileReader(path.toFile()), JsonObject.class);
            ModConfig config = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e-> {});

            config.loadConfigResources();
            config.loadVariantResources();

            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkExistenceOrSave() throws IOException {
        Path path = Path.of(FULL_PATH);
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(path)) {
            Files.createFile(path);
            ModConfig config = defaultConfig();
            FileWriter writer = new FileWriter(FULL_PATH);
            JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE,config).getOrThrow(false, e->{});
            GSON.toJson(json, writer);
            writer.flush();
            writer.close();
        }
    }

    private void loadConfigResources() {
        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "configs", rl->true);

        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID,rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(optional.get()), JsonObject.class);
                    try {
                        ConfigResource resource = ConfigResource.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> {
                        });
                        this.configResource.addFrom(resource);
                    } catch (RuntimeException e) {
                        ExcavatedVariants.LOGGER.error("Issues loading resource: {}", rl, e);
                    }
                }
            }
        }
    }

    private void loadVariantResources() {
        Map<ResourceLocation, ModData> modMap = new HashMap<>();

        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "variants", rl->true);
        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID,rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(optional.get()), JsonObject.class);
                    try {
                        ModData resource = ModData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e->{});
                        modMap.put(rl, resource);
                    } catch (RuntimeException e) {
                        ExcavatedVariants.LOGGER.error("Issues loading resource: {}", rl, e);
                    }
                }
            }
        }

        for (ResourceLocation rl : this.configResource.priority) {
            ResourceLocation newRl = new ResourceLocation(rl.getNamespace(), "variants/"+rl.getPath()+".json");
            ModData data = modMap.get(newRl);
            if (data!=null) {
                this.mods.add(data);
                modMap.remove(newRl);
            }
        }

        this.mods.addAll(modMap.values());
    }
}
