/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import com.google.auto.service.AutoService;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@AutoService(ResourceProvider.class)
public class DefaultProvider implements ResourceProvider {

    @Override
    public @Nullable List<ModelData> provideStoneTextures(Stone stone, ResourceGenerationContext context) {
        List<ResourceLocation> blockModels = getBlockModels(stone.block, context);
        if (blockModels == null || blockModels.isEmpty()) {
            ExcavatedVariants.LOGGER.warn("Could not find blockstates for stone " + stone);
            return null;
        }
        List<ModelData> models = new ArrayList<>();
        for (ResourceLocation model : blockModels) {
            try {
                models.add(ParsedModel.getFromLocation(model, context).makeStoneModel());
            } catch (IOException ignored) {}
        }
        if (!models.isEmpty()) {
            return models;
        }
        return null;
    }

    @Override
    public @Nullable List<TexFaceProvider> provideOreTextures(Ore ore, ResourceKey<Block> selectedBlock, ResourceGenerationContext context) {
        /*
         * - Reading in the blockstates of ore and stone involved
         * - From each blockstate, getting a list of models involved - this will be a *single list*, not a key-value
         *   map, as we only care about the "primary" model and its variants (and we assume that each orientation uses
         *   the same "primary" model and variants).
         * - Now we have a list of models per blockstate. For each model:
         *    - extract the textures used in the model
         */
        List<ResourceLocation> blockModels = getBlockModels(selectedBlock, context);
        if (blockModels == null || blockModels.isEmpty()) {
            ExcavatedVariants.LOGGER.warn("Could not find blockstates for ore " + ore);
            return null;
        }
        List<TexFaceProvider> models = new ArrayList<>();
        for (ResourceLocation model : blockModels) {
            try {
                models.add(ParsedModel.getFromLocation(model, context).makeTextureProvider());
            } catch (IOException ignored) {}
        }
        if (!models.isEmpty()) {
            return models;
        }
        return null;
    }

    private List<ResourceLocation> getBlockModels(ResourceKey<Block> block, ResourceGenerationContext context) {
        BlockModelDefinition.Context ctx = new BlockModelDefinition.Context();
        try (InputStream blockstateStream = BackupFetcher.getBlockStateFile(block.location(), context)) {
            BlockModelDefinition definition = BlockModelDefinition.fromStream(ctx, new BufferedReader(new InputStreamReader(blockstateStream)));
            if (!definition.isMultiPart()) {
                Set<ResourceLocation> oreModels = new HashSet<>();
                for (Map.Entry<String, MultiVariant> entry : definition.getVariants().entrySet()) {
                    oreModels.addAll(entry.getValue().getDependencies());
                }
                return List.copyOf(oreModels);
            } else {
                return List.of();
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public int priority() {
        return ExcavatedVariants.DEFAULT_COMPAT_PRIORITY;
    }
}
