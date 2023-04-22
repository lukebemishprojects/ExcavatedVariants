package dev.lukebemish.excavatedvariants.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProviderMap;
import dev.lukebemish.excavatedvariants.platform.Services;

import net.minecraft.resources.ResourceLocation;

public class ResourceCollector {
    private static final List<ResourceProvider> PROVIDERS = Services.COMPAT.getClientListeners(ResourceProvider.class);

    static Map<ResourceLocation, List<ModelData>> makeStoneTextures(List<ResourceLocation> stones) {
        Map<ResourceLocation, List<ModelData>> stoneTextures = new HashMap<>();
        PROVIDERS.forEach(provider -> provider.provideStoneTextures(stones, stoneTextures::put));
        return stoneTextures;
    }

    static Map<ResourceLocation, List<TexFaceProviderMap>> makeOreTextures(List<ResourceLocation> ores) {
        Map<ResourceLocation, List<TexFaceProviderMap>> oreTextures = new HashMap<>();
        PROVIDERS.forEach(provider -> provider.provideOreTextures(ores, oreTextures::put));
        return oreTextures;
    }
}
