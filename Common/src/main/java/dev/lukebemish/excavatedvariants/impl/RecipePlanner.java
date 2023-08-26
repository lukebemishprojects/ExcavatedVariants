/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecipePlanner implements PathAwareInputStreamSource {
    public final Map<TagKey<Block>, ResourceKey<Block>> oreToBaseOreMap = new HashMap<>();
    private final Map<ResourceLocation, TagKey<Block>> recipeToTagMap = new HashMap<>();
    private boolean initialized = false;
    private synchronized void initialize() {
        if (initialized) return;
        oreToBaseOreMap.keySet()
                .forEach(tagKey -> {
                    var location = new ResourceLocation(
                            ExcavatedVariants.MOD_ID,
                            "recipes/ore_conversion/" + tagKey.location().getNamespace() + "/" + tagKey.location().getPath() + ".json"
                    );
                    recipeToTagMap.put(location, tagKey);
                });
        initialized = true;
    }

    @Override
    public @NotNull Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
        initialize();
        return recipeToTagMap.keySet();
    }

    @Override
    public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        initialize();
        var tagKey = recipeToTagMap.get(outRl);
        if (tagKey == null) return null;
        var blockKey = oreToBaseOreMap.get(tagKey);
        if (blockKey == null) return null;
        String recipe = String.format(
                "{\"type\":\"minecraft:crafting_shapeless\",\"ingredients\":[{\"tag\":\"%s\"}],\"result\":{\"item\":\"%s\"}}",
                tagKey.location(), blockKey.location()
        );
        return () -> new ByteArrayInputStream(recipe.getBytes());
    }

    // Don't bother caching
}
