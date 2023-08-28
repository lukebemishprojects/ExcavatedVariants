/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSource;

import java.util.function.Function;

/**
 * Can provide a texture given a method for wrapping texture sources. This should be used to wrap any direct texture
 * reader, in order to support animations.
 */
public interface NamedTextureProvider extends Function<TextureProducer.SourceWrapper, TexSource> {
}
