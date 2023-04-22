package dev.lukebemish.excavatedvariants.api.client;

import java.util.List;
import java.util.function.Supplier;

import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource;

import net.minecraft.resources.ResourceLocation;

public interface NamedTextureProvider extends Supplier<ITexSource> {
    List<ResourceLocation> getUsedTextures();
}
