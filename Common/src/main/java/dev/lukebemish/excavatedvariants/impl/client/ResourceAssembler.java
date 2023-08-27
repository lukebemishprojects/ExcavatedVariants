/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.dynamicassetgenerator.api.InputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSource;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureGenerator;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureMetaGenerator;
import dev.lukebemish.excavatedvariants.api.client.*;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ResourceAssembler implements PathAwareInputStreamSource {
    private final Map<ResourceKey<Stone>, List<ModelData>> stoneModels = new HashMap<>();
    private final Map<ResourceKey<Ore>, List<TexFaceProvider>> oreModels = new HashMap<>();
    private final Map<ResourceLocation, InputStreamSource> resources = new HashMap<>();
    private final Map<ResourceLocation, String> cacheKeys = new HashMap<>();
    private final Map<ResourceKey<Stone>, String> stoneCacheKeys = new HashMap<>();
    private final Map<ResourceKey<Ore>, String> oreCacheKeys = new HashMap<>();

    public void addFuture(ExcavatedVariants.VariantFuture future, ResourceGenerationContext context) {
        if (!stoneModels.containsKey(future.stone.getKeyOrThrow())) {
            Mutable<StringBuilder> cacheExtraBuilder = new MutableObject<>(new StringBuilder());
            var textures = ResourceCollector.makeStoneTextures(future.stone, context, s -> {
                if (s != null && cacheExtraBuilder.getValue() != null) {
                    cacheExtraBuilder.getValue().append(s);
                } else {
                    cacheExtraBuilder.setValue(null);
                }
            });
            if (textures != null) {
                stoneModels.put(future.stone.getKeyOrThrow(), textures);
                if (cacheExtraBuilder.getValue() != null) {
                    String built = cacheExtraBuilder.getValue().toString();
                    if (!built.isEmpty()) stoneCacheKeys.put(future.stone.getKeyOrThrow(), built);
                }
            }
        }
        if (!stoneModels.containsKey(future.foundSourceStone.getKeyOrThrow())) {
            Mutable<StringBuilder> cacheExtraBuilder = new MutableObject<>(new StringBuilder());
            var textures = ResourceCollector.makeStoneTextures(future.foundSourceStone, context, s -> {
                if (s != null && cacheExtraBuilder.getValue() != null) {
                    cacheExtraBuilder.getValue().append(s);
                } else {
                    cacheExtraBuilder.setValue(null);
                }
            });
            if (textures != null) {
                stoneModels.put(future.foundSourceStone.getKeyOrThrow(), textures);
                if (cacheExtraBuilder.getValue() != null) {
                    String built = cacheExtraBuilder.getValue().toString();
                    if (!built.isEmpty()) stoneCacheKeys.put(future.foundSourceStone.getKeyOrThrow(), built);
                }
            }
        }
        if (!oreModels.containsKey(future.ore.getKeyOrThrow())) {
            Mutable<StringBuilder> cacheExtraBuilder = new MutableObject<>(new StringBuilder());
            var textures = ResourceCollector.makeOreTextures(future.ore, future.foundOreKey, context, s -> {
                if (s != null && cacheExtraBuilder.getValue() != null) {
                    cacheExtraBuilder.getValue().append(s);
                } else {
                    cacheExtraBuilder.setValue(null);
                }
            });
            if (textures != null) {
                oreModels.put(future.ore.getKeyOrThrow(), textures);
                if (cacheExtraBuilder.getValue() != null) {
                    String built = cacheExtraBuilder.getValue().toString();
                    if (!built.isEmpty()) oreCacheKeys.put(future.ore.getKeyOrThrow(), built);
                }
            }
        }

        processPair(future, context);
    }

    private void processPair(ExcavatedVariants.VariantFuture future, ResourceGenerationContext context) {
        List<ModelData> oldStoneModels = stoneModels.get(future.foundSourceStone.getKeyOrThrow());
        List<ModelData> newStoneModels = stoneModels.get(future.stone.getKeyOrThrow());
        List<TexFaceProvider> oreModels = this.oreModels.get(future.ore.getKeyOrThrow());

        if (oldStoneModels == null) {
            ExcavatedVariants.LOGGER.warn("No existing stone models found for "+future.foundSourceStone.getKeyOrThrow());
            return;
        }
        if (newStoneModels == null) {
            ExcavatedVariants.LOGGER.warn("No new stone models found for "+future.stone.getKeyOrThrow());
            return;
        }
        if (oreModels == null || oreModels.isEmpty()) {
            ExcavatedVariants.LOGGER.warn("No ore models found for "+future.ore.getKeyOrThrow());
            return;
        }

        String oldStoneCacheKey = stoneCacheKeys.get(future.foundSourceStone.getKeyOrThrow());
        String newStoneCacheKey = stoneCacheKeys.get(future.stone.getKeyOrThrow());
        String oreCacheKey = oreCacheKeys.get(future.ore.getKeyOrThrow());

        String cacheKey = (oldStoneCacheKey != null && newStoneCacheKey != null && oreCacheKey != null)
                ? stoneCacheKeys.get(future.foundSourceStone.getKeyOrThrow()) + "\n"
                + stoneCacheKeys.get(future.stone.getKeyOrThrow()) + "\n"
                + oreCacheKeys.get(future.ore.getKeyOrThrow())
                : null;

        ModelData oldStoneModel = oldStoneModels.get(0);

        int counter = 0;
        List<ResourceLocation> models = new ArrayList<>();
        for (ModelData newStoneModel : newStoneModels) {
            for (TexFaceProvider oreModel : oreModels) {
                ResourceLocation modelLocation = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/"+future.fullId+"__"+counter);
                assembleModel(modelLocation, oreModel, oldStoneModel, newStoneModel, future.foundSourceStone, cacheKey, context);
                models.add(modelLocation);
                counter += 1;
            }
        }

        // Generate blockstate file
        var fullId = future.fullId;
        ModifiedOreBlock block = ExcavatedVariants.BLOCKS.get(future);

        var assembled = BlockStateData.create(block, models);
        var encoded = BlockStateData.CODEC.encodeStart(JsonOps.INSTANCE, assembled).result();
        if (encoded.isPresent()) {
            var json = ExcavatedVariants.GSON_CONDENSED.toJson(encoded.get());
            addResource(new ResourceLocation(ExcavatedVariants.MOD_ID, "blockstates/"+fullId+".json"),
                    (resourceLocation, c) -> () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        } else {
            ExcavatedVariants.LOGGER.warn("Failed to encode blockstate for "+fullId);
        }
    }

    private void assembleModel(ResourceLocation modelLocation, TexFaceProvider ore, ModelData oldStone, ModelData newStone, Stone oldStoneData, String cacheKey, ResourceGenerationContext context) {
        Map<String, StoneTexFace> stoneFaceLocationMap = new HashMap<>();
        Map<String, ResourceLocation> modelTextureTranslations = new HashMap<>();
        NamedTextureProvider[] oldStoneTexSource = new NamedTextureProvider[1];

        oldStone.produceTextures((name, texture, faces) -> oldStoneTexSource[0] = texture);

        if (oldStoneTexSource[0] == null) {
            ExcavatedVariants.LOGGER.warn("No existing stone texture found for "+oldStoneData.getKeyOrThrow().location());
            return;
        }

        int[] counter = new int[] {0};
        newStone.produceTextures((name, texture, faces) -> {
            counter[0] += 1;
            ResourceLocation location = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath()+"__"+counter[0]);
            modelTextureTranslations.put(name, location);

            if (faces.isEmpty()) {
                return;
            }
            stoneFaceLocationMap.put(name, new StoneTexFace(new HashSet<>(faces), location, texture));
        });

        // Make the actual model here...
        JsonElement model = newStone.assembleModel(Collections.unmodifiableMap(modelTextureTranslations));
        ResourceLocation modelJsonLocation = new ResourceLocation(modelLocation.getNamespace(), "models/"+modelLocation.getPath()+".json");
        addResource(modelJsonLocation, (resourceLocation, c) -> () -> new ByteArrayInputStream(model.toString().getBytes(StandardCharsets.UTF_8)));

        // TODO: handle cache key

        // And now we'll generate the ore textures
        for (Map.Entry<String, StoneTexFace> entry : stoneFaceLocationMap.entrySet()) {
            StoneTexFace stoneTexFace = entry.getValue();
            Set<Face> faces = stoneTexFace.faces();
            TextureProducer oreTexture = ore.get(faces.stream().findFirst().get());
            assembleTextures(stoneTexFace.textureLocation(), oreTexture, oldStoneTexSource[0], stoneTexFace.texture(), cacheKey, context);
        }
    }

    private void assembleTextures(ResourceLocation output, TextureProducer oreTexture, NamedTextureProvider oldStoneTexture, NamedTextureProvider newStoneTexture, String cacheKey, ResourceGenerationContext context) {
        List<ResourceLocation> usedLocations = new ArrayList<>();

        var oreTextureResult = oreTexture.produce(newStoneTexture, oldStoneTexture);
        TexSource outTexture = oreTextureResult.getFirst();
        usedLocations.addAll(oreTextureResult.getSecond());
        usedLocations.addAll(oldStoneTexture.getUsedTextures());
        usedLocations.addAll(newStoneTexture.getUsedTextures());
        flattenResources(new TextureMetaGenerator.Builder().withOutputLocation(output).withSources(usedLocations).build(), cacheKey, context);
        flattenResources(new TextureGenerator(output, outTexture), cacheKey, context);
    }

    private void flattenResources(PathAwareInputStreamSource source, String cacheKey, ResourceGenerationContext context) {
        for (ResourceLocation location : source.getLocations(context)) {
            resources.put(location, source);
            if (cacheKey != null) {
                String cacheKeyForLocation = source.createCacheKey(location, context);
                if (cacheKeyForLocation != null) {
                    cacheKeys.put(location, Services.PLATFORM.getModVersion()+":"+cacheKey + "\n" + cacheKeyForLocation);
                }
            }
        }
    }

    private void addResource(ResourceLocation location, InputStreamSource source) {
        resources.put(location, source);
    }

    private record StoneTexFace(Set<Face> faces, ResourceLocation textureLocation, NamedTextureProvider texture) {}

    @Override
    public @NotNull Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
        return resources.keySet();
    }

    @Override
    public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        var supplier = resources.get(outRl);
        if (supplier == null) return null;
        return supplier.get(outRl, context);
    }

    @Override
    public @Nullable String createCacheKey(ResourceLocation outRl, ResourceGenerationContext context) {
        return cacheKeys.get(outRl);
    }
}
