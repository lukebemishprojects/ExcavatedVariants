/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;

import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.resources.ResourceLocation;

@ExcavatedVariantsListener
public class DefaultProvider implements ResourceProvider {
    @Override
    public void provideOreTextures(List<ResourceLocation> ores, BiConsumer<ResourceLocation, List<TexFaceProvider>> textureProducerConsumer) {
        /*
         * - Reading in the blockstates of every ore and stone involved
         * - From each blockstate, getting a list of models involved - this will be a *single list*, not a key-value
         *   map, as we only care about the "primary" model and its variants (and we assume that each orientation uses
         *   the same "primary" model and variants).
         * - Now we have a list of models per blockstate. For each model:
         *    - extract the textures used in the model
         */
        for (ResourceLocation ore : ores) {
            List<ResourceLocation> blockModels = getBlockModels(ore);
            if (blockModels == null || blockModels.isEmpty()) {
                ExcavatedVariants.LOGGER.warn("Could not find blockstates for ore " + ore);
                continue;
            }
            List<TexFaceProvider> models = new ArrayList<>();
            for (ResourceLocation model : blockModels) {
                try {
                    models.add(ParsedModel.getFromLocation(model).makeTextureProvider());
                } catch (IOException ignored) {}
            }
            if (!models.isEmpty()) {
                textureProducerConsumer.accept(ore, models);
            }
        }
    }

    @Override
    public void provideStoneTextures(List<ResourceLocation> stones, BiConsumer<ResourceLocation, List<ModelData>> textureConsumer) {
        for (ResourceLocation stone : stones) {
            List<ResourceLocation> blockModels = getBlockModels(stone);
            if (blockModels == null || blockModels.isEmpty()) {
                ExcavatedVariants.LOGGER.warn("Could not find blockstates for stone " + stone);
                continue;
            }
            List<ModelData> models = new ArrayList<>();
            for (ResourceLocation model : blockModels) {
                try {
                    models.add(ParsedModel.getFromLocation(model).makeStoneModel());
                } catch (IOException ignored) {}
            }
            if (!models.isEmpty()) {
                textureConsumer.accept(stone, models);
            }
        }
    }

    private List<ResourceLocation> getBlockModels(ResourceLocation block) {
        BlockModelDefinition.Context ctx = new BlockModelDefinition.Context();
        try (InputStream blockstateStream = BackupFetcher.getBlockStateFile(block)) {
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
