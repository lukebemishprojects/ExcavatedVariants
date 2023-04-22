package dev.lukebemish.excavatedvariants.impl.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;
import dev.lukebemish.excavatedvariants.impl.platform.Services;

import net.minecraft.resources.ResourceLocation;

public class ResourceCollector {
    private static final List<ResourceProvider> PROVIDERS = Services.COMPAT.getClientListeners(ResourceProvider.class);

    static Map<ResourceLocation, List<ModelData>> makeStoneTextures(List<ResourceLocation> stones) {
        Map<ResourceLocation, List<ModelData>> stoneTextures = new HashMap<>();
        PROVIDERS.forEach(provider -> provider.provideStoneTextures(stones, stoneTextures::put));
        return stoneTextures;
    }

    static Map<ResourceLocation, List<TexFaceProvider>> makeOreTextures(List<ResourceLocation> ores) {
        Map<ResourceLocation, List<TexFaceProvider>> oreTextures = new HashMap<>();
        PROVIDERS.forEach(provider -> provider.provideOreTextures(ores, oreTextures::put));
        return oreTextures;
    }
}
