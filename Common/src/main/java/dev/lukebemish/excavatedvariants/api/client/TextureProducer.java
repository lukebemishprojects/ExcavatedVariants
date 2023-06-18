/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSource;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

/**
 * A function which produces a new ore texture source from the given texture sources.
 */
@FunctionalInterface
public interface TextureProducer {
    /**
     * Produces a new ore texture source from the given texture sources.
     * @param newStoneSource the texture source of the stone to be transferred to
     * @param oldStoneSource the texture source of the stone to be transferred from
     * @return a pair of the new texture source and a list of the resource locations of textures used in generating the
     * new texture source
     */
    Pair<TexSource, List<ResourceLocation>> produce(Function<SourceWrapper, TexSource> newStoneSource, Function<SourceWrapper, TexSource> oldStoneSource);

    interface SourceWrapper {
        TexSource wrap(TexSource source);
    }
}
