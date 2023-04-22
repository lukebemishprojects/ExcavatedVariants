package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TexFaceProviderMap {
    @NotNull TextureProducer get(Face face);
}
