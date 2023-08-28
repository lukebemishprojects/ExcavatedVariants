/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MappingsCache {
    public static final Codec<MappingsCache> CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf()).xmap(MappingsCache::new, (data) -> data.mappings);

    private final Map<String, List<ResourceLocation>> mappings;

    public MappingsCache(Map<String, List<ResourceLocation>> mappings) {
        this.mappings = new HashMap<>(mappings);
    }

    public @NotNull List<ResourceLocation> get(String id) {
        return Collections.unmodifiableList(mappings.getOrDefault(id, List.of()));
    }

    private static final Path CACHE_PATH = Services.PLATFORM.getModDataFolder().resolve("mappings_cache.json");

    public static MappingsCache load() {
        if (!Files.exists(CACHE_PATH)) {
            return new MappingsCache(Map.of());
        }
        try (var reader = Files.newBufferedReader(CACHE_PATH, StandardCharsets.UTF_8)) {
            JsonElement element = ExcavatedVariants.GSON.fromJson(reader, JsonElement.class);
            return CODEC.parse(JsonOps.INSTANCE, element)
                    .mapError(s -> {
                        ExcavatedVariants.LOGGER.error("Failed to parse mappings cache: {}", s);
                        return s;
                    }).result().orElseThrow(() -> new IOException("Failed to parse mappings cache"));
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Failed to load mappings cache (loading default)", e);
            return new MappingsCache(Map.of());
        }
    }

    public void update() {
        for (ExcavatedVariants.VariantFuture future : ExcavatedVariants.COMPLETE_VARIANTS) {
            List<ResourceLocation> outBlocks = new ArrayList<>();
            outBlocks.add(future.foundOreKey.location());
            for (ResourceKey<Block> ore : future.ore.getOriginalBlocks().keySet()) {
                if (ore == future.foundOreKey) continue;
                outBlocks.add(ore.location());
            }
            outBlocks.add(future.stone.block.location());
            mappings.put(future.fullId, outBlocks);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CACHE_PATH.getParent());
            try (var writer = Files.newBufferedWriter(CACHE_PATH, StandardCharsets.UTF_8)) {
                JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, this).mapError(s -> {
                    ExcavatedVariants.LOGGER.error("Failed to encode mappings cache: {}", s);
                    return s;
                }).result().orElseThrow(() -> new IOException("Failed to encode mappings cache"));
                ExcavatedVariants.GSON_PRETTY.toJson(json, writer);
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Failed to save mappings cache", e);
        }
    }
}
