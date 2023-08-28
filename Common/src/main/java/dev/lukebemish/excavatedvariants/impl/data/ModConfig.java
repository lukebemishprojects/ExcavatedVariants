/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final Path CONFIG_PATH = Services.PLATFORM.getConfigFolder();
    public static final Path FULL_PATH = CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID + ".json");
    public static final Codec<ModConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("attempt_worldgen_replacement").forGetter(c -> c.attemptWorldgenReplacement),
                    Codec.BOOL.fieldOf("add_conversion_recipes").forGetter(c -> c.addConversionRecipes)
            ).apply(instance, ModConfig::new));

    public final boolean attemptWorldgenReplacement;
    public final boolean addConversionRecipes;

    private ModConfig(boolean attemptWorldgenReplacement, boolean addConversionRecipes) {
        this.attemptWorldgenReplacement = attemptWorldgenReplacement;
        this.addConversionRecipes = addConversionRecipes;
    }

    private static ModConfig defaultConfig() {
        return new ModConfig(true, true);
    }

    public static ModConfig load() {
        if (!Files.exists(FULL_PATH)) {
            ModConfig config = defaultConfig();
            write(config);
            return config;
        }
        try (var reader = Files.newBufferedReader(FULL_PATH, StandardCharsets.UTF_8)) {
            JsonElement json = ExcavatedVariants.GSON.fromJson(reader, JsonElement.class);
            return CODEC.parse(JsonOps.INSTANCE, json).mapError(s -> {
                ExcavatedVariants.LOGGER.error("Failed to parse config: {}", s);
                return s;
            }).result().orElseThrow(() -> new IOException("Failed to parse config"));
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Failed to read config", e);
            ModConfig config = defaultConfig();
            write(config);
            return config;
        }
    }

    private static void write(ModConfig config) {
        try (var writer = Files.newBufferedWriter(FULL_PATH, StandardCharsets.UTF_8)) {
            JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, config).mapError(s -> {
                ExcavatedVariants.LOGGER.error("Failed to encode config: {}", s);
                return s;
            }).result().orElseThrow(() -> new IOException("Failed to encode config"));
            writer.write(ExcavatedVariants.GSON_PRETTY.toJson(json));
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Failed to write config", e);
        }
    }
}
