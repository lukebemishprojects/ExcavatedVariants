package io.github.lukebemish.excavated_variants.data;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.codecutils.api.CommentedCodec;
import io.github.lukebemish.codecutils.api.JanksonOps;
import io.github.lukebemish.codecutils.api.TomlConfigOps;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModConfig {
    public static final TomlParser TOML_PARSER = TomlFormat.instance().createParser();
    public static final TomlWriter TOML_WRITER = TomlFormat.instance().createWriter();
    static {
        TOML_WRITER.setIndent(IndentStyle.SPACES_2);
    }
    public static final Jankson JANKSON = Jankson.builder().build();
    public static final Path CONFIG_PATH = Services.PLATFORM.getConfigFolder();
    public static final Path FULL_PATH = CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID+".toml");

    public static final Codec<ModConfig> CODEC = CommentedCodec.of(RecordCodecBuilder.<ModConfig>create(instance->instance.group(
            Codec.BOOL.fieldOf("attempt_ore_gen_insertion").forGetter(c->c.attemptOreGenInsertion),
            Codec.BOOL.fieldOf("attempt_worldgen_replacement").forGetter(c->c.attemptWorldgenReplacement),
            Codec.BOOL.fieldOf("add_conversion_recipes").forGetter(c->c.addConversionRecipes),
            Codec.BOOL.fieldOf("jei_rei_compat").forGetter(c->c.jeiReiCompat),
            Codec.BOOL.fieldOf("unobtainable_variants").forGetter(c->c.unobtainableVariants)
    ).apply(instance,ModConfig::new)))
            .comment("Allows the world-generation changes to be toggled. Useful if another tool, such as KubeJS, is " +
                    "being used for ore gen. Is a bit less reliable than attempt_worldgen_replacement.", "attempt_ore_gen_insertion")
            .comment("Toggles the much slower, but more reliable, ore-gen changes. Disable to speed up world generation " +
                    "substantially at the cost of less-reliable replacement of the original ore with its variants. If this is disabled, " +
                    "some ores will not be replaced correctly.", "attempt_worldgen_replacement")
            .comment("Toggles whether to add recipes to convert variants back to the base ore.", "add_conversion_recipes")
            .comment("Toggles compatibility with JEI and REI for added conversion recipes.", "jei_rei_compat")
            .comment("If this is on, variants will drop the base ore, even with silk touch.", "unobtainable_variants");

    public final boolean attemptOreGenInsertion;
    public final boolean attemptWorldgenReplacement;
    public final boolean addConversionRecipes;
    public final boolean jeiReiCompat;
    public final boolean unobtainableVariants;

    public final ConfigResource configResource = ConfigResource.empty();

    public final List<ModData> mods = new ArrayList<>();

    public final List<VariantModifier> modifiers = new ArrayList<>();

    private ModConfig(boolean attempt_ore_gen_insertion, boolean attempt_worldgen_replacement, boolean add_conversion_recipes, boolean jei_rei_compat,
                     boolean unobtainable_variants) {
        this.attemptOreGenInsertion = attempt_ore_gen_insertion;
        this.attemptWorldgenReplacement = attempt_worldgen_replacement;
        this.addConversionRecipes = add_conversion_recipes;
        this.jeiReiCompat = jei_rei_compat;
        this.unobtainableVariants = unobtainable_variants;
    }

    private static ModConfig defaultConfig() {
        return new ModConfig(true, true, true, true, false);
    }

    public static ModConfig load() {
        try {
            checkExistenceOrSave();

            Config toml = TOML_PARSER.parse(new FileReader(FULL_PATH.toFile()));
            ModConfig config = CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e-> {});

            config.loadConfigResources();
            config.loadVariantResources();
            config.loadVariantModifiers();

            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkExistenceOrSave() throws IOException {
        ResourceProvider.forceInitialization();
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(FULL_PATH)) {
            Files.createFile(FULL_PATH);
            ModConfig config = defaultConfig();
            var writer = Files.newBufferedWriter(FULL_PATH);
            Config toml = (Config) CODEC.encodeStart(TomlConfigOps.INSTANCE, config).getOrThrow(false, e->{});
            TOML_WRITER.write(toml.unmodifiable(), writer);
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
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = JANKSON.load(optional.get());
                            ConfigResource resource = ConfigResource.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {});
                            this.configResource.addFrom(resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            ConfigResource resource = ConfigResource.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {});
                            this.configResource.addFrom(resource);
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        ExcavatedVariants.LOGGER.error("Issues loading resource: {}", rl, e);
                    }
                }
            }
        }
    }

    private void loadVariantModifiers() {
        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "modifiers", rl->true);

        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID,rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = JANKSON.load(optional.get());
                            VariantModifier resource = VariantModifier.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {});
                            this.modifiers.add(resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            VariantModifier resource = VariantModifier.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {});
                            this.modifiers.add(resource);
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
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
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = JANKSON.load(optional.get());
                            ModData resource = ModData.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {});
                            modMap.put(rl, resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            ModData resource = ModData.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {});
                            modMap.put(rl, resource);
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        ExcavatedVariants.LOGGER.error("Issues loading resource: {}", rl, e);
                    }
                }
            }
        }

        for (ResourceLocation rl : this.configResource.priority) {
            ResourceLocation newRl = new ResourceLocation(rl.getNamespace(), "variants/"+rl.getPath());
            ModData data = modMap.get(newRl);
            if (data!=null) {
                this.mods.add(data);
                modMap.remove(newRl);
            }
        }

        this.mods.addAll(modMap.values());
    }
}
