/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ResourceCollector {
    private static final List<ResourceProvider> PROVIDERS = Services.loadListeners(ResourceProvider.class);

    static @Nullable List<ModelData> makeStoneTextures(Stone stone, ResourceGenerationContext context) {
        for (var provider : PROVIDERS) {
            var models = provider.provideStoneTextures(stone, context);
            if (models != null) {
                return models;
            }
        }
        return null;
    }

    static @Nullable List<TexFaceProvider> makeOreTextures(Ore ore, ResourceKey<Block> selectedBlock, ResourceGenerationContext context) {
        for (var provider : PROVIDERS) {
            var models = provider.provideOreTextures(ore, selectedBlock, context);
            if (models != null) {
                return models;
            }
        }
        return null;
    }
}
