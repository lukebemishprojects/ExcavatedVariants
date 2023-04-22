package dev.lukebemish.excavatedvariants.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.dynamicassetgenerator.api.IInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.IResourceGenerator;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureGenerator;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureMetaGenerator;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.api.client.Face;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.NamedTextureProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;
import dev.lukebemish.excavatedvariants.api.client.TextureProducer;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;

public class ResourceAssembler implements IPathAwareInputStreamSource {
    private final Map<String, BaseStone> originalPairs = new HashMap<>();
    private final List<Pair<BaseOre, BaseStone>> toMake;

    private final Map<String, ResourceLocation> oreBlocks = new HashMap<>();
    private final Map<String, ResourceLocation> stoneBlocks = new HashMap<>();
    private final Map<ResourceLocation, List<ModelData>> stoneModels;
    private final Map<ResourceLocation, List<TexFaceProvider>> oreModels;

    private final Map<ResourceLocation, IInputStreamSource> resources = new HashMap<>();

    public ResourceAssembler(Map<BaseOre, BaseStone> originalPairs, List<Pair<BaseOre, BaseStone>> toMake) {
        for (Map.Entry<BaseOre, BaseStone> entry : originalPairs.entrySet()) {
            this.originalPairs.put(entry.getKey().id, entry.getValue());
        }
        this.toMake = toMake;

        for (Map.Entry<BaseOre, BaseStone> pair : originalPairs.entrySet()) {
            BaseOre ore = pair.getKey();
            BaseStone stone = pair.getValue();
            oreBlocks.put(ore.id, ore.blockId.get(0));
            stoneBlocks.put(stone.id, stone.blockId);
        }

        for (Pair<BaseOre, BaseStone> pair : toMake) {
            BaseOre ore = pair.getFirst();
            BaseStone stone = pair.getSecond();
            oreBlocks.put(ore.id, ore.blockId.get(0));
            stoneBlocks.put(stone.id, stone.blockId);
        }

        stoneModels = ResourceCollector.makeStoneTextures(List.copyOf(stoneBlocks.values()));
        oreModels = ResourceCollector.makeOreTextures(List.copyOf(oreBlocks.values()));
    }

    /*
     * Main entrypoint for the resource assembler - calls everything else
     */
    public void assemble() {
        /*
         * First, we need to assemble the data we need. This involves:
         *  - use that texture data to generate new textures
         *  - ...and then generate new models from this.
         *  - Finally, we generate a single new blockstate per pair which uses the new models.
         */
        for (Pair<BaseOre, BaseStone> pair : toMake) {
            BaseOre ore = pair.getFirst();
            BaseStone newStone = pair.getSecond();
            BaseStone oldStone = originalPairs.get(ore.id);
            if (oldStone == null) {
                ExcavatedVariants.LOGGER.warn("Ore "+ore.id+" has no existing stone variant to extract from!");
                continue;
            }
            processPair(ore, oldStone, newStone);
        }
    }

    private void processPair(BaseOre ore, BaseStone oldStone, BaseStone newStone) {
        List<ModelData> oldStoneModels = stoneModels.get(stoneBlocks.get(oldStone.id));
        List<ModelData> newStoneModels = stoneModels.get(stoneBlocks.get(newStone.id));
        List<TexFaceProvider> oreModels = this.oreModels.get(oreBlocks.get(ore.id));

        if (oldStoneModels == null) {
            ExcavatedVariants.LOGGER.warn("No existing stone models found for "+oldStone.id);
            return;
        }
        if (newStoneModels == null) {
            ExcavatedVariants.LOGGER.warn("No new stone models found for "+newStone.id);
            return;
        }
        if (oreModels == null || oreModels.isEmpty()) {
            ExcavatedVariants.LOGGER.warn("No ore models found for "+ore.id);
            return;
        }

        ModelData oldStoneModel = oldStoneModels.get(0);

        int counter = 0;
        List<ResourceLocation> models = new ArrayList<>();
        for (ModelData newStoneModel : newStoneModels) {
            for (TexFaceProvider oreModel : oreModels) {
                ResourceLocation modelLocation = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/"+ore.id+"__"+oldStone.id+"__"+counter);
                assembleModel(modelLocation, oreModel, oldStoneModel, newStoneModel, oldStone);
                models.add(modelLocation);
                counter += 1;
            }
        }

        // Generate blockstate file
        var fullId = newStone.id + "_" + ore.id;
        ModifiedOreBlock block = ExcavatedVariants.getBlocks().get(fullId);

        var assembled = BlockStateData.create(block, models);
        var encoded = BlockStateData.CODEC.encodeStart(JsonOps.INSTANCE, assembled).result();
        if (encoded.isPresent()) {
            var json = ExcavatedVariants.GSON_CONDENSED.toJson(encoded.get());
            resources.put(new ResourceLocation(ExcavatedVariants.MOD_ID, "blockstates/"+fullId+".json"),
                    (resourceLocation, context) -> () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        } else {
            ExcavatedVariants.LOGGER.warn("Failed to encode blockstate for "+fullId);
        }
    }

    private void assembleModel(ResourceLocation modelLocation, TexFaceProvider ore, ModelData oldStone, ModelData newStone, BaseStone oldStoneData) {
        Map<String, StoneTexFace> stoneFaceLocationMap = new HashMap<>();
        Map<String, ResourceLocation> modelTextureTranslations = new HashMap<>();
        NamedTextureProvider[] oldStoneTexSource = new NamedTextureProvider[1];

        oldStone.produceTextures((name, texture, faces) -> oldStoneTexSource[0] = texture);

        if (oldStoneTexSource[0] == null) {
            ExcavatedVariants.LOGGER.warn("No existing stone texture found for "+oldStoneData.id);
            return;
        }

        int[] counter = new int[] {0};
        newStone.produceTextures((name, texture, faces) -> {
            counter[0] += 1;
            ResourceLocation location = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath()+"_"+counter[0]);
            modelTextureTranslations.put(name, location);

            if (faces.isEmpty()) {
                return;
            }
            stoneFaceLocationMap.put(name, new StoneTexFace(new HashSet<>(faces), location, texture));
        });

        // Make the actual model here...
        JsonElement model = newStone.assembleModel(Collections.unmodifiableMap(modelTextureTranslations));
        ResourceLocation modelJsonLocation = new ResourceLocation(modelLocation.getNamespace(), "models/"+modelLocation.getPath()+".json");
        resources.put(modelJsonLocation, (resourceLocation, context) -> () -> new ByteArrayInputStream(model.toString().getBytes(StandardCharsets.UTF_8)));

        // And now we'll generate the ore textures
        for (Map.Entry<String, StoneTexFace> entry : stoneFaceLocationMap.entrySet()) {
            StoneTexFace stoneTexFace = entry.getValue();
            Set<Face> faces = stoneTexFace.faces();
            TextureProducer oreTexture = ore.get(faces.stream().findFirst().get());
            assembleTextures(stoneTexFace.textureLocation(), oreTexture, oldStoneTexSource[0], stoneTexFace.texture());
        }
    }

    @Override
    public @NotNull Set<ResourceLocation> getLocations() {
        return resources.keySet();
    }

    @Override
    public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        return resources.get(outRl).get(outRl, context);
    }

    private record StoneTexFace(Set<Face> faces, ResourceLocation textureLocation, NamedTextureProvider texture) {}

    private void assembleTextures(ResourceLocation output, TextureProducer oreTexture, NamedTextureProvider oldStoneTexture, NamedTextureProvider newStoneTexture) {
        List<ResourceLocation> usedLocations = new ArrayList<>();

        var oreTextureResult = oreTexture.produce(newStoneTexture, oldStoneTexture);
        ITexSource outTexture = oreTextureResult.getFirst().cached();
        usedLocations.addAll(oreTextureResult.getSecond());
        usedLocations.addAll(oldStoneTexture.getUsedTextures());
        usedLocations.addAll(newStoneTexture.getUsedTextures());
        processGenerator(new TextureMetaGenerator(
                usedLocations,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                output));
        processGenerator(new TextureGenerator(output, outTexture));
    }

    private void processGenerator(IResourceGenerator generator) {
        for (ResourceLocation location : generator.getLocations()) {
            resources.put(location, generator);
        }
    }
}
