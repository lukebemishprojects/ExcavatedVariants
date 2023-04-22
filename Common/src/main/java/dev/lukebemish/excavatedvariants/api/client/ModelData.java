package dev.lukebemish.excavatedvariants.api.client;

import java.util.Collection;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

public interface ModelData {
    JsonObject assembleModel(Map<String, ResourceLocation> textures);

    void produceTextures(TextureConsumer textureProducerConsumer);

    @FunctionalInterface
    interface TextureConsumer {
        void accept(String textureName, NamedTextureProvider texture, Collection<Face> providedFaces);
    }
}
