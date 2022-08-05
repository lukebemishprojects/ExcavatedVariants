package io.github.lukebemish.excavated_variants.client;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.IInputStreamSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.AssetResourceCache;
import io.github.lukebemish.dynamic_asset_generator.api.client.ClientPrePackRepository;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockStateAssembler {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    public static Map<ResourceLocation, Supplier<InputStream>> getMap(TextureRegistrar registrar, Collection<Pair<BaseOre, BaseStone>> originalPairs, List<Pair<BaseOre, BaseStone>> toMake) {
        BlockModelDefinition.Context ctx = new BlockModelDefinition.Context();
        Map<ResourceLocation, Supplier<InputStream>> resources = new HashMap<>();
        Map<String, Pair<BlockModelDefinition, List<Pair<BlockModelHolder, List<List<ResourceLocation>>>>>> oreInfoMap = new HashMap<>();
        Map<String, Pair<BlockModelDefinition, List<Pair<BlockModelHolder, List<List<ResourceLocation>>>>>> stoneInfoMap = new HashMap<>();
        Map<String, List<Pair<List<ResourceLocation>, List<ResourceLocation>>>> oreStonePairMap = new HashMap<>();
        for (Pair<BaseOre, BaseStone> p : toMake) {
            var ore = p.getFirst();
            var stone = p.getSecond();
            try {
                if (!oreInfoMap.containsKey(ore.id)) {
                    ResourceLocation oreRl = ore.block_id.get(0);
                    oreInfoMap.put(ore.id, getInfoFromBlockstate(oreRl, ctx));
                }

                if (!stoneInfoMap.containsKey(stone.id)) {
                    ResourceLocation stoneRl = stone.block_id;
                    stoneInfoMap.put(stone.id, getInfoFromBlockstate(stoneRl, ctx));
                }
            } catch (IOException e) {
                //TODO: add stuff here
                //Potentially ignore this; it could be we're running too early.
                ExcavatedVariants.LOGGER.info(e);
                //throw new RuntimeException(e);
            }
        }
        for (Pair<BaseOre, BaseStone> p : originalPairs) {
            var ore = p.getFirst();
            var base = p.getSecond();
            try {
                if (!oreInfoMap.containsKey(ore.id)) {
                    ResourceLocation oreRl = ore.block_id.get(0);
                    oreInfoMap.put(ore.id, getInfoFromBlockstate(oreRl, ctx));
                }

                if (!stoneInfoMap.containsKey(base.id)) {
                    ResourceLocation stoneRl = base.block_id;
                    stoneInfoMap.put(base.id, getInfoFromBlockstate(stoneRl, ctx));
                }

                var stoneInfo = stoneInfoMap.get(base.id);
                var oreInfo = oreInfoMap.get(ore.id);

                if (stoneInfo != null && oreInfo != null && stoneInfo.getSecond().size() >= 1 && oreInfo.getSecond().size() >= 1) {
                    ArrayList<Pair<List<ResourceLocation>, List<ResourceLocation>>> oreStonePairs = new ArrayList<>();
                    oreStonePairMap.put(ore.id, oreStonePairs);
                    var holders = oreInfo.getSecond();
                    for (var holder : holders) {
                        for (var rls : holder.getSecond()) {
                            oreStonePairs.add(new Pair<>(
                                    rls,
                                    stoneInfo.getSecond().get(0).getSecond().get(0)
                            ));
                        }
                    }
                } else {
                    ExcavatedVariants.LOGGER.warn("Bad info while extracting from blocks {} and {}:{}", ore.block_id.get(0).toString(), base.block_id.toString(),
                            (stoneInfo == null ? "\nMissing stone block model info" : "") +
                                    (oreInfo == null ? "\nMissing ore block model info" : "") +
                                    (stoneInfo != null && (stoneInfo.getSecond().size() < 1) ? "\nNo stone textures found" : "") +
                                    (oreInfo != null && (oreInfo.getSecond().size() < 1) ? "\nNo ore textures found" : ""));
                }

            } catch (IOException e) {
                //TODO: add stuff here
                //Potentially ignore this; it could be we're running too early.
                ExcavatedVariants.LOGGER.info(e);
                //throw new RuntimeException(e);
            } catch (JsonSyntaxException e) {
                //TODO: add other stuff here
                ExcavatedVariants.LOGGER.error(e);
                //throw new RuntimeException(e);
            }
        }

        for (Pair<BaseOre, BaseStone> p : toMake) {
            var oreInfo = oreInfoMap.get(p.getFirst().id);
            var stoneInfo = stoneInfoMap.get(p.getSecond().id);
            var oreStonePair = oreStonePairMap.get(p.getFirst().id);
            var stone = p.getSecond();
            var ore = p.getFirst();
            var fullId = stone.id + "_" + ore.id;

            var outBlock = ExcavatedVariants.getBlocks().get(fullId);

            if (oreInfo != null && stoneInfo != null && oreStonePair != null && outBlock != null) {
                // stone, ore -> new
                Map<Pair<ResourceLocation, ResourceLocation>, ResourceLocation> texMap = new HashMap<>();
                // First, create new textures:
                int index = 0;
                for (var stoneBG : stoneInfo.getSecond().stream().flatMap(pair -> pair.getSecond().stream()).collect(Collectors.toSet())) {
                    for (var exp : oreStonePair) {
                        ResourceLocation outRL = new ResourceLocation(ExcavatedVariants.MOD_ID, "textures/block/" + fullId + index + ".png");
                        ResourceLocation outRlInternal = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/" + fullId + index);
                        Pair<IInputStreamSource, IInputStreamSource> sources = registrar.setupExtractor(exp.getSecond(), exp.getFirst(), stoneBG.get(0), outRlInternal);
                        resources.put(outRL,
                                () -> sources.getFirst().get(outRL).get());
                        resources.put(new ResourceLocation(outRL.getNamespace(), outRL.getPath() + ".mcmeta"),
                                () -> sources.getSecond().get(new ResourceLocation(outRL.getNamespace(), outRL.getPath() + ".mcmeta")).get());
                        index++;
                        texMap.put(new Pair<>(stoneBG.get(0), exp.getFirst().get(0)), outRlInternal);
                        stoneBG.stream().skip(1).forEach(stoneRl -> {
                            exp.getSecond().stream().skip(1).forEach(oreRl -> texMap.put(new Pair<>(stoneRl, oreRl), AssetResourceCache.EMPTY_TEXTURE));
                        });
                    }
                }

                // Next, process the models:
                var defaultModels = oreInfo.getFirst().getMultiVariants().stream().findFirst().get().getVariants()
                        .stream().map(Variant::getModelLocation).toList();
                var stoneModel = stoneInfo.getFirst().getMultiVariants().stream().findFirst().get().getVariants()
                        .stream().map(Variant::getModelLocation).findFirst().get();
                var outModelRLs = new ArrayList<ResourceLocation>();
                try {
                    InputStream read;
                    try {
                        read = ClientPrePackRepository.getResource(new ResourceLocation(stoneModel.getNamespace(), "models/" + stoneModel.getPath() + ".json"));
                    } catch (IOException e) {
                        read = BackupFetcher.provideBlockModelFile(stoneModel);
                    }
                    BlockModelParser parentModel = BlockModelParser.readModel(new BufferedReader(new InputStreamReader(read, StandardCharsets.UTF_8)));
                    List<ResourceLocation> stoneLocs = new ArrayList<>(parentModel.textures.values().stream().map(x -> ResourceLocation.of(x, ':')).toList());
                    if (parentModel.children != null)
                        stoneLocs.addAll(parentModel.children.values().stream().flatMap(parser -> parser.textures.values().stream())
                                .map(x -> ResourceLocation.of(x, ':')).toList());

                    int i2 = 0;
                    for (ResourceLocation m : defaultModels) {
                        InputStream stoneRead;
                        try {
                            stoneRead = ClientPrePackRepository.getResource(new ResourceLocation(stoneModel.getNamespace(), "models/" + stoneModel.getPath() + ".json"));
                        } catch (IOException e) {
                            stoneRead = BackupFetcher.provideBlockModelFile(stoneModel);
                        }
                        BlockModelParser outputModel = BlockModelParser.readModel(new BufferedReader(new InputStreamReader(stoneRead, StandardCharsets.UTF_8)));
                        try {
                            stoneRead = ClientPrePackRepository.getResource(new ResourceLocation(stoneModel.getNamespace(), "models/" + stoneModel.getPath() + ".json"));
                        } catch (IOException e) {
                            stoneRead = BackupFetcher.provideBlockModelFile(stoneModel);
                        }
                        JsonObject outputMap = GSON.fromJson(new BufferedReader(new InputStreamReader(stoneRead, StandardCharsets.UTF_8)), JsonObject.class);
                        InputStream oreRead;
                        try {
                            oreRead = ClientPrePackRepository.getResource(new ResourceLocation(m.getNamespace(), "models/" + m.getPath() + ".json"));
                        } catch (IOException e) {
                            oreRead = BackupFetcher.provideBlockModelFile(m);
                        }
                        StringBuilder oreTextBuilder = new StringBuilder();
                        Reader oreReader = new BufferedReader(new InputStreamReader
                                (oreRead, Charset.forName(StandardCharsets.UTF_8.name())));
                        int oreC = 0;
                        while ((oreC = oreReader.read()) != -1) {
                            oreTextBuilder.append((char) oreC);
                        }
                        BlockModelParser oreModel = BlockModelParser.readModel(oreTextBuilder.toString());
                        BlockModelHolder oreModelHolder = BlockModelHolder.getFromString(oreTextBuilder.toString());
                        Set<ResourceLocation> oreLocs = extractTextures(oreModelHolder);
                        ResourceLocation mainOreTex = oreLocs.stream().filter(x ->
                                        !stoneLocs.contains(x) && texMap.entrySet().stream().anyMatch(y ->
                                                y.getKey().getSecond().equals(x) && !y.getValue().equals(AssetResourceCache.EMPTY_TEXTURE)))
                                .findFirst().orElse(oreLocs.stream().filter(x ->
                                                texMap.entrySet().stream().anyMatch(y ->
                                                        y.getKey().getSecond().equals(x) && !y.getValue().equals(AssetResourceCache.EMPTY_TEXTURE)))
                                        .findFirst().orElse(null));

                        remapTextures(texMap, outputModel, outputMap, mainOreTex);

                        texMap.values().stream().filter(rl -> !AssetResourceCache.EMPTY_TEXTURE.equals(rl)).findFirst().ifPresent(rl -> {
                            setParticleTexture(outputMap, rl);
                        });

                        var outModelRL = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/" + fullId + i2);
                        outModelRLs.add(outModelRL);
                        String finalJson = GSON.toJson(outputMap);
                        resources.put(new ResourceLocation(ExcavatedVariants.MOD_ID, "models/" + outModelRL.getPath() + ".json"),
                                () -> new ByteArrayInputStream(finalJson.getBytes()));
                        i2++;
                    }

                    // Create output BS
                    var assembler = BlockstateModelParser.create(outBlock, outModelRLs);
                    String bs = GSON.toJson(assembler);
                    resources.put(new ResourceLocation(ExcavatedVariants.MOD_ID, "blockstates/" + fullId + ".json"),
                            () -> new ByteArrayInputStream(bs.getBytes()));
                } catch (IOException e) {
                    //TODO: add stuff here
                    ExcavatedVariants.LOGGER.error(e);
                    //throw new RuntimeException(e);
                }
            } else {
                ExcavatedVariants.LOGGER.warn("Missing {}for ore {}", "" +
                        (oreInfo == null ? "ore model info, " : "") +
                        (stoneInfo == null ? "stone model info, " : "") +
                        (oreStonePair == null ? "texture extractor info, " : "") +
                        (outBlock == null ? "registered block, " : ""), fullId);
            }
        }

        return resources;
    }

    private static Set<ResourceLocation> extractTextures(BlockModelHolder oreModelHolder) {
        Set<ResourceLocation> rls = new HashSet<>(oreModelHolder.getResolvedTextureMap().values());
        if (oreModelHolder.children().isPresent()) {
            oreModelHolder.children().get().values().forEach(holder -> rls.addAll(extractTextures(holder)));
        }
        return rls;
    }

    private static void setParticleTexture(JsonObject outputMap, ResourceLocation particle) {
        if (outputMap.has("textures") && outputMap.get("textures") instanceof JsonObject textures) {
            textures.add("particle", new JsonPrimitive(particle.toString()));
        }
        if (outputMap.has("children") && outputMap.get("children") instanceof JsonObject children) {
            for (String key : children.keySet()) {
                if (children.get(key) instanceof JsonObject jsonObject)
                    setParticleTexture(jsonObject, particle);
            }
        }
    }

    private static void remapTextures(Map<Pair<ResourceLocation, ResourceLocation>, ResourceLocation> texMap, BlockModelParser outputModel, JsonObject outputMap, ResourceLocation mainOreTex) {
        if (outputModel.textures != null) {
            for (String s : outputModel.textures.keySet()) {
                String val = outputModel.textures.get(s);
                ResourceLocation old = ResourceLocation.of(val, ':');
                ResourceLocation lookup = texMap.get(new Pair<>(old, mainOreTex));
                if (lookup != null) outputModel.replaceTexture(old, lookup);
            }
            outputMap.add("textures", GSON.toJsonTree(outputModel.textures));
        }

        if (outputModel.children != null && outputMap.has("children")
                && outputMap.get("children") instanceof JsonObject jsonObject
                && jsonObject.keySet().equals(outputModel.children.keySet())) {
            JsonArray newJsonArray = new JsonArray();
            for (String key : jsonObject.keySet()) {
                BlockModelParser modelE = outputModel.children.get(key);
                JsonElement mapE = jsonObject.get(key);
                if (mapE instanceof JsonObject modelMapObject) {
                    remapTextures(texMap, modelE, modelMapObject, mainOreTex);
                    newJsonArray.add(mapE);
                }
            }
            outputMap.add("children", newJsonArray);
        }
    }

    private static Pair<BlockModelDefinition, List<Pair<BlockModelHolder, List<List<ResourceLocation>>>>> getInfoFromBlockstate(ResourceLocation oreRl, BlockModelDefinition.Context ctx) throws IOException {
        ResourceLocation oreBS = new ResourceLocation(oreRl.getNamespace(), "blockstates/" + oreRl.getPath() + ".json");
        InputStream oreBSIS;
        try {
            oreBSIS = ClientPrePackRepository.getResource(oreBS);
        } catch (IOException e) {
            oreBSIS = BackupFetcher.provideBlockstateFile(oreRl);
        }
        BlockModelDefinition oreBMD = BlockModelDefinition.fromStream(ctx, new BufferedReader(new InputStreamReader(oreBSIS, StandardCharsets.UTF_8)));

        if (!oreBMD.isMultiPart()) {
            Set<ResourceLocation> oreModels = new HashSet<>();
            for (Map.Entry<String, MultiVariant> e : oreBMD.getVariants().entrySet()) {
                oreModels.addAll(e.getValue().getDependencies());
            }

            List<Pair<BlockModelHolder, List<List<ResourceLocation>>>> modelsList = new ArrayList<>();
            for (ResourceLocation mRl : oreModels) {
                BlockModelHolder holder = BlockModelHolder.getFromLocation(mRl);
                if (holder == null) continue;
                modelsList.add(createOverlayStack(holder));
            }
            return new Pair<>(oreBMD, modelsList);
        }
        return null;
    }

    private static Pair<BlockModelHolder, List<List<ResourceLocation>>> createOverlayStack(
            BlockModelHolder holder) {
        return new Pair<>(
                holder,
                holder.setupOverlays());
    }
}
