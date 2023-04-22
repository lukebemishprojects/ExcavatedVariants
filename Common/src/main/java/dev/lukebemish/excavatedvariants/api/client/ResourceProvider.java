package dev.lukebemish.excavatedvariants.api.client;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;

public interface ResourceProvider extends ClientListener {
    /**
     * Provides stone textures to the resource collector.
     * @param textureConsumer consumes pairs of block resource locations and model lists.
     */
    default void provideStoneTextures(List<ResourceLocation> stones, BiConsumer<ResourceLocation, List<ModelData>> textureConsumer) {}

    /**
     * Provides ore textures to the resource collector.
     * @param textureProducerConsumer consumes pairs of block resource locations and model lists.
     */
    default void provideOreTextures(List<ResourceLocation> ores, BiConsumer<ResourceLocation, List<TexFaceProvider>> textureProducerConsumer) {}

}
