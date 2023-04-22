package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TexFaceProvider {
    @NotNull TextureProducer get(Face face);
}
