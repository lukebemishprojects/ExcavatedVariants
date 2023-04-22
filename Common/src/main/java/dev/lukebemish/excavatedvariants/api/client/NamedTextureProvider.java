package dev.lukebemish.excavatedvariants.api.client;

import java.util.List;
import java.util.function.Function;

import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource;

import net.minecraft.resources.ResourceLocation;

/**
 * Can both provide a texture given a method for wrapping texture sources (this should be used to wrap any direct
 * texture reader, in order to support animations), and provide a list of all textures used by this provider.
 */
public interface NamedTextureProvider extends Function<TextureProducer.SourceWrapper, ITexSource> {
    List<ResourceLocation> getUsedTextures();
}
