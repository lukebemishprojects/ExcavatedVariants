/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.defaultresources.api.GlobalResourceManager;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.codecs.CommentedCodec;
import dev.lukebemish.excavatedvariants.impl.codecs.TomlConfigOps;
import dev.lukebemish.excavatedvariants.impl.platform.Services;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

            return CODEC.parse(TomlConfigOps.INSTANCE, toml).getOrThrow(false, e -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkExistenceOrSave() throws IOException {
        GlobalResourceManager.forceInitialization();
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
}
