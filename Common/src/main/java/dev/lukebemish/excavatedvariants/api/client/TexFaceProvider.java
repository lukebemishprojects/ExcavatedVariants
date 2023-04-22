package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the model of an ore block; used to provide a (@link TextureProducer} for any face of the block.
 */
@FunctionalInterface
public interface TexFaceProvider {
    @NotNull TextureProducer get(Face face);
}
