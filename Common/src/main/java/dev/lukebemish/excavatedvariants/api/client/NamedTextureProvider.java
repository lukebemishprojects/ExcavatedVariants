/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSource;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

/**
 * Can both provide a texture given a method for wrapping texture sources (this should be used to wrap any direct
 * texture reader, in order to support animations), and provide a list of all textures used by this provider.
 */
public interface NamedTextureProvider extends Function<TextureProducer.SourceWrapper, TexSource> {
    List<ResourceLocation> getUsedTextures();
}
