/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import java.util.Collection;
import java.util.Map;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

/**
 * A structure which provides data about a stone model; it should be able to produce textures, during which it should
 * emit a series of string keys, each corresponding to a single texture, as well as a {@link NamedTextureProvider} which
 * can produce that texture, and a collection of {@link Face}s which that texture appears on. It will later be asked to
 * assemble a full model given a map of the same texture string keys provided earlier to the actual texture locations.
 */
public interface ModelData {
    JsonElement assembleModel(Map<String, ResourceLocation> textures);

    void produceTextures(TextureConsumer textureProducerConsumer);

    @FunctionalInterface
    interface TextureConsumer {
        void accept(String textureName, NamedTextureProvider texture, Collection<Face> providedFaces);
    }
}
