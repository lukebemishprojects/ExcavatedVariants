/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ResourceProvider extends ClientListener {
    /**
     * Provides stone textures to the resource collector.
     */
    default @Nullable List<ModelData> provideStoneTextures(Stone stone, ResourceGenerationContext context) {
        return null;
    }

    /**
     * Provides ore textures to the resource collector.
     */
    default @Nullable List<TexFaceProvider> provideOreTextures(Ore ore, ResourceKey<Block> selectedBlock, ResourceGenerationContext context) {
        return null;
    }

}
