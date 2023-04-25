/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the model of an ore block; used to provide a (@link TextureProducer} for any face of the block.
 */
@FunctionalInterface
public interface TexFaceProvider {
    @NotNull TextureProducer get(Face face);
}
