/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.dynamicassetgenerator.api.InputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.api.client.*;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
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
    private final TextureAtlasBuilder textureAtlasBuilder = new TextureAtlasBuilder();

    public void addFuture(ExcavatedVariants.VariantFuture future, ResourceGenerationContext context) {
        if (!stoneModels.containsKey(future.stone.getKeyOrThrow())) {

            var textures = ResourceCollector.makeStoneTextures(future.stone, context);
            if (textures != null) {
                stoneModels.put(future.stone.getKeyOrThrow(), textures);
            }
        }
        if (!stoneModels.containsKey(future.foundSourceStone.getKeyOrThrow())) {
            var textures = ResourceCollector.makeStoneTextures(future.foundSourceStone, context);
            if (textures != null) {
                stoneModels.put(future.foundSourceStone.getKeyOrThrow(), textures);
            }
        }
        if (!oreModels.containsKey(future.ore.getKeyOrThrow())) {
            var textures = ResourceCollector.makeOreTextures(future.ore, future.foundOreKey, context);
            if (textures != null) {
                oreModels.put(future.ore.getKeyOrThrow(), textures);
            }
        }

        processPair(future);
    }

    private void processPair(ExcavatedVariants.VariantFuture future) {
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

        ModelData oldStoneModel = oldStoneModels.get(0);

        int counter = 0;
        List<ResourceLocation> models = new ArrayList<>();
        for (ModelData newStoneModel : newStoneModels) {
            for (TexFaceProvider oreModel : oreModels) {
                ResourceLocation modelLocation = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/"+future.fullId+"__"+counter);
                assembleModel(modelLocation, oreModel, oldStoneModel, newStoneModel, future.foundSourceStone);
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
            var json = ExcavatedVariants.GSON.toJson(encoded.get());
            addResource(new ResourceLocation(ExcavatedVariants.MOD_ID, "blockstates/"+fullId+".json"),
                    (resourceLocation, c) -> () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        } else {
            ExcavatedVariants.LOGGER.warn("Failed to encode blockstate for "+fullId);
        }
    }

    private void assembleModel(ResourceLocation modelLocation, TexFaceProvider ore, ModelData oldStone, ModelData newStone, Stone oldStoneData) {
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

        // And now we'll generate the ore textures
        for (Map.Entry<String, StoneTexFace> entry : stoneFaceLocationMap.entrySet()) {
            StoneTexFace stoneTexFace = entry.getValue();
            Set<Face> faces = stoneTexFace.faces();
            // note that when a StoneTexFace is constructed it always has at least one face
            //noinspection OptionalGetWithoutIsPresent
            TextureProducer oreTexture = ore.get(faces.stream().findFirst().get());
            assembleTextures(stoneTexFace.textureLocation(), oreTexture, oldStoneTexSource[0], stoneTexFace.texture());
        }
    }

    private void assembleTextures(ResourceLocation output, TextureProducer oreTexture, NamedTextureProvider oldStoneTexture, NamedTextureProvider newStoneTexture) {
        var outTexture = oreTexture.produce(newStoneTexture, oldStoneTexture);
        textureAtlasBuilder.addSource(output, outTexture);
    }

    private void addResource(ResourceLocation location, InputStreamSource source) {
        resources.put(location, source);
    }

    private record StoneTexFace(Set<Face> faces, ResourceLocation textureLocation, NamedTextureProvider texture) {}

    @Override
    public @NotNull Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
        return Sets.union(resources.keySet(), textureAtlasBuilder.getLocations(context));
    }

    @Override
    public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        var supplier = resources.get(outRl);
        if (supplier != null) return supplier.get(outRl, context);
        if (textureAtlasBuilder.getLocations(context).contains(outRl))
            return textureAtlasBuilder.get(outRl, context);
        return null;
    }

    @Override
    public @Nullable String createCacheKey(ResourceLocation outRl, ResourceGenerationContext context) {
        if (textureAtlasBuilder.getLocations(context).contains(outRl))
            return textureAtlasBuilder.createCacheKey(outRl, context);
        return null;
    }
}
