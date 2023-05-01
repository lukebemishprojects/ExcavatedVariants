/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.defaultresources.api.ResourceProvider;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.codecs.CommentedCodec;
import dev.lukebemish.excavatedvariants.impl.codecs.JanksonOps;
import dev.lukebemish.excavatedvariants.impl.codecs.TomlConfigOps;
import dev.lukebemish.excavatedvariants.impl.data.filter.Filter;
import dev.lukebemish.excavatedvariants.impl.data.modifier.Flag;
import dev.lukebemish.excavatedvariants.impl.data.modifier.VariantModifier;
import dev.lukebemish.excavatedvariants.impl.platform.Services;

import net.minecraft.resources.ResourceLocation;

public class ModConfig {
    public static final TomlParser TOML_PARSER = TomlFormat.instance().createParser();
    public static final TomlWriter TOML_WRITER = TomlFormat.instance().createWriter();
    public static final Path CONFIG_PATH = Services.PLATFORM.getConfigFolder();
    public static final Path FULL_PATH = CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID + ".toml");
    public static final Codec<ModConfig> CODEC = CommentedCodec.of(RecordCodecBuilder.<ModConfig>create(instance -> instance.group(
                    Codec.BOOL.fieldOf("attempt_worldgen_replacement").forGetter(c -> c.attemptWorldgenReplacement),
                    Codec.BOOL.fieldOf("add_conversion_recipes").forGetter(c -> c.addConversionRecipes),
                    Codec.BOOL.fieldOf("jei_rei_compat").forGetter(c -> c.jeiReiCompat)
            ).apply(instance, ModConfig::new)))
            .comment("Toggles ore-gen changes; without this, ores won't be replaced during world gen.", "attempt_worldgen_replacement")
            .comment("Toggles whether to add recipes to convert variants back to the base ore.", "add_conversion_recipes")
            .comment("Toggles compatibility with JEI and REI for added conversion recipes.", "jei_rei_compat");

    static {
        TOML_WRITER.setIndent(IndentStyle.SPACES_2);
    }

    public final boolean attemptWorldgenReplacement;
    public final boolean addConversionRecipes;
    public final boolean jeiReiCompat;

    public final ConfigResource configResource = ConfigResource.empty();

    public final List<ModData> mods = new ArrayList<>();

    public final List<VariantModifier> modifiers = new ArrayList<>();

    public final Flags flags = new Flags();

    private ModConfig(boolean attemptWorldgenReplacement, boolean addConversionRecipes, boolean jeiReiCompat) {
        this.attemptWorldgenReplacement = attemptWorldgenReplacement;
        this.addConversionRecipes = addConversionRecipes;
        this.jeiReiCompat = jeiReiCompat;
    }

    private static ModConfig defaultConfig() {
        return new ModConfig(true, true, true);
    }

    public static ModConfig load() {
        try {
            checkExistenceOrSave();

            Config toml = TOML_PARSER.parse(new FileReader(FULL_PATH.toFile()));
            ModConfig config = CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {
            });

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
            Config toml = (Config) CODEC.encodeStart(TomlConfigOps.INSTANCE, config).getOrThrow(false, e -> {
            });
            TOML_WRITER.write(toml.unmodifiable(), writer);
            writer.flush();
            writer.close();
        }
    }

    private void loadConfigResources() {
        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "configs", rl -> true);

        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = ExcavatedVariants.JANKSON.load(optional.get());
                            ConfigResource resource = ConfigResource.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
                            });
                            this.configResource.addFrom(resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            ConfigResource resource = ConfigResource.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {
                            });
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
        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "modifiers", rl -> true);

        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = ExcavatedVariants.JANKSON.load(optional.get());
                            VariantModifier resource = VariantModifier.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
                            });
                            this.modifiers.add(resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            VariantModifier resource = VariantModifier.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {
                            });
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

        var rls = ResourceProvider.instance().getResources(ExcavatedVariants.MOD_ID, "variants", rl -> true);
        for (var rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(ExcavatedVariants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst();
                if (optional.isPresent()) {
                    try {
                        if (rl.getPath().endsWith(".json") || rl.getPath().endsWith(".json5")) {
                            JsonObject json = ExcavatedVariants.JANKSON.load(optional.get());
                            ModData resource = ModData.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
                            });
                            modMap.put(rl, resource);
                        } else if (rl.getPath().endsWith(".toml")) {
                            Config toml = TOML_PARSER.parse(new InputStreamReader(optional.get()));
                            ModData resource = ModData.CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {
                            });
                            modMap.put(rl, resource);
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        ExcavatedVariants.LOGGER.error("Issues loading resource: {}", rl, e);
                    }
                }
            }
        }

        for (ResourceLocation rl : this.configResource.priority) {
            ResourceLocation newRl = new ResourceLocation(rl.getNamespace(), "variants/" + rl.getPath());
            ModData data = modMap.get(newRl);
            if (data != null) {
                this.mods.add(data);
                modMap.remove(newRl);
            }
        }

        this.mods.addAll(modMap.values());
    }

    public class Flags {
        final Supplier<List<Pair<Filter, List<Flag>>>> flags;

        private Flags() {
            flags = Suppliers.memoize(() ->
                    modifiers.stream().filter(m -> m.flags().isPresent())
                            .map(m -> new Pair<>(m.filter(), m.flags().get())).toList()
            );
        }

        public Set<Flag> getFlags(String ore, String stone) {
            return flags.get().stream()
                    .filter(p -> p.getFirst().matches(ore, stone))
                    .flatMap(p -> p.getSecond().stream())
                    .collect(Sets.toImmutableEnumSet());
        }

        public Set<Flag> getFlags(BaseOre ore, BaseStone stone) {
            return getFlags(ore.id, stone.id);
        }
    }
}
