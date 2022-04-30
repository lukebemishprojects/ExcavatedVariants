package com.github.lukebemish.excavated_variants.client;

import com.github.lukebemish.dynamic_asset_generator.client.api.ClientPrePackRepository;
import com.github.lukebemish.dynamic_asset_generator.client.api.DynAssetGeneratorClientAPI;
import com.github.lukebemish.dynamic_asset_generator.client.api.ForegroundTransferType;
import com.github.lukebemish.dynamic_asset_generator.client.api.PaletteExtractor;
import com.github.lukebemish.dynamic_asset_generator.client.util.IPalettePlan;
import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;
import com.github.lukebemish.excavated_variants.util.Triple;
import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class BlockStateAssembler {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    public static void setupClientAssets(Collection<Pair<BaseOre,BaseStone>> original_pairs, List<Pair<BaseOre,BaseStone>> to_make) {
        int_original_pairs=original_pairs;
        int_to_make=to_make;
        for (Pair<BaseOre,BaseStone> p : to_make) {
            var full_id = p.last().id+"_"+p.first().id;
            //null textures *should* just be ignored. In theory, since they won't ever be referenced...
            int max_tex_num = p.first().texture_count*p.last().texture_count;
            ArrayList<ResourceLocation> targets = new ArrayList<>(List.of(
                    new ResourceLocation(ExcavatedVariants.MOD_ID, "blockstates/" + full_id + ".json")
            ));
            for (int i = 0; i<max_tex_num; i++) {
                targets.add(new ResourceLocation(ExcavatedVariants.MOD_ID,"models/block/"+full_id+i+".json"));
                targets.add(new ResourceLocation(ExcavatedVariants.MOD_ID, "textures/block/"+full_id+i+".png"));
            }
            for (ResourceLocation rl : targets) {
                DynAssetGeneratorClientAPI.planLoadingStream(rl,new ResettingHolder(rl));
            }
        }

    }

    private static Map<ResourceLocation,Supplier<InputStream>> intMap ;
    private static Collection<Pair<BaseOre,BaseStone>> int_original_pairs;
    private static List<Pair<BaseOre,BaseStone>> int_to_make;

    public static Map<ResourceLocation,Supplier<InputStream>> updateMap() {
        if (intMap==null) {
            intMap = getMap(int_original_pairs,int_to_make);
        }
        return intMap==null?new HashMap<>():intMap;
    }

    public static void reset() {
        intMap = null;
    }

    private static Map<ResourceLocation,Supplier<InputStream>> getMap(Collection<Pair<BaseOre,BaseStone>> original_pairs, List<Pair<BaseOre,BaseStone>> to_make) {
        BlockModelDefinition.Context ctx = new BlockModelDefinition.Context();
        Map<ResourceLocation,Supplier<InputStream>> resources = new HashMap<>();
        Map<String,Pair<BlockModelDefinition,List<ResourceLocation>>> oreInfoMap = new HashMap<>();
        Map<String,Pair<BlockModelDefinition,List<ResourceLocation>>> stoneInfoMap = new HashMap<>();
        Map<String, ArrayList<Triple<PaletteExtractor,ResourceLocation,Boolean>>> extractorMap = new HashMap<>();
        for (Pair<BaseOre,BaseStone> p : to_make) {
            var ore = p.first();
            var stone = p.last();
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
        for (Pair<BaseOre,BaseStone> p : original_pairs) {
            var ore = p.first();
            var base = p.last();
            try {
                if (!oreInfoMap.containsKey(ore.id)) {
                    ResourceLocation oreRl = ore.block_id.get(0);
                    oreInfoMap.put(ore.id,getInfoFromBlockstate(oreRl,ctx));
                }

                if (!stoneInfoMap.containsKey(base.id)) {
                    ResourceLocation stoneRl = base.block_id;
                    stoneInfoMap.put(base.id,getInfoFromBlockstate(stoneRl,ctx));
                }

                var stoneInfo = stoneInfoMap.get(base.id);
                var oreInfo = oreInfoMap.get(ore.id);

                if (stoneInfo!=null && oreInfo!=null && stoneInfo.last().size()>=1 && oreInfo.last().size()>=1) {
                    ArrayList<Triple<PaletteExtractor,ResourceLocation,Boolean>> extractors = new ArrayList<>();
                    extractorMap.put(ore.id,extractors);
                    for (ResourceLocation rl : oreInfo.last()) {
                        var is = ClientPrePackRepository.getResource(rl);
                        var img = NativeImage.read(is);
                        boolean isTransparent = false;
                        outer:
                        for (int x = 0; x < img.getWidth(); x++) {
                            for (int y = 0; y < img.getHeight(); y++) {
                                int c = img.getPixelRGBA(x, y);
                                float alpha = (c >> 24 & 255) / 255.0F;
                                if (alpha < 0.5f) {
                                    isTransparent = true;
                                    break outer;
                                }
                            }
                        }

                        PaletteExtractor extractor = isTransparent?null:new PaletteExtractor(stoneInfo.last().get(0),rl, 6,true,true,0.2).fillHoles(true);
                        extractors.add(new Triple<>(extractor,rl,!isTransparent));
                    }
                } else {
                    ExcavatedVariants.LOGGER.warn("Bad info while extracting from blocks {} and {}:{}",ore.block_id.get(0).toString(),base.block_id.toString(),
                            (stoneInfo==null?"\nMissing stone block model info":"")+
                                    (oreInfo==null?"\nMissing ore block model info":"")+
                                    (stoneInfo!=null&&(stoneInfo.last().size()<1)?"\nNo stone textures found":"")+
                                    (oreInfo!=null&&(oreInfo.last().size()<1)?"\nNo ore textures found":""));
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

        for (Pair<BaseOre,BaseStone> p : to_make) {
            var oreInfo = oreInfoMap.get(p.first().id);
            var stoneInfo = stoneInfoMap.get(p.last().id);
            var extractorPair = extractorMap.get(p.first().id);
            var stone = p.last();
            var ore = p.first();
            var full_id = stone.id + "_" + ore.id;

            var outBlock = ExcavatedVariants.getBlocks().get(full_id);

            if (oreInfo!=null && stoneInfo!=null && extractorPair!=null && outBlock!=null) {
                // stone, ore -> new
                Map<Pair<ResourceLocation,ResourceLocation>,ResourceLocation> texMap = new HashMap<>();
                // First, create new textures:
                int index = 0;
                for (ResourceLocation stoneBG : stoneInfo.last()) {
                    for (Triple<PaletteExtractor,ResourceLocation,Boolean> exp : extractorPair) {
                        if (exp.last()) {
                            IPalettePlan plan = new ForegroundTransferType(exp.first(), stoneBG,
                                    true, false);
                            ResourceLocation outRL = new ResourceLocation(ExcavatedVariants.MOD_ID, "textures/block/" + full_id + index + ".png");
                            index++;
                            resources.put(outRL, plan.getStream(outRL));
                            texMap.put(new Pair<>(stoneBG,exp.second()), outRL);
                        } else {
                            texMap.put(new Pair<>(stoneBG,exp.second()), exp.second());
                        }
                    }
                }

                HashMap<Pair<ResourceLocation,ResourceLocation>,ResourceLocation> newTexMap = new HashMap<>();
                for (Pair<ResourceLocation,ResourceLocation> pair : texMap.keySet()) {
                    var p1 = pair.first().getPath().substring(9);
                    p1 = p1.substring(0,p1.length()-4);
                    var rl1 = new ResourceLocation(pair.first().getNamespace(),p1);
                    p1 = pair.last().getPath().substring(9);
                    p1 = p1.substring(0,p1.length()-4);
                    var rl2 = new ResourceLocation(pair.last().getNamespace(),p1);
                    p1 = texMap.get(pair).getPath().substring(9);
                    p1 = p1.substring(0,p1.length()-4);
                    var rl3 = new ResourceLocation(texMap.get(pair).getNamespace(),p1);
                    newTexMap.put(new Pair<>(rl1,rl2),rl3);
                }

                // Next, process the models:
                var defaultModels = oreInfo.first().getMultiVariants().stream().findFirst().get().getVariants()
                        .stream().map(Variant::getModelLocation).toList();
                var stoneModel = stoneInfo.first().getMultiVariants().stream().findFirst().get().getVariants()
                        .stream().map(Variant::getModelLocation).findFirst().get();
                var outModelRLs = new ArrayList<ResourceLocation>();
                try {
                    InputStream read;
                    try {
                        read = ClientPrePackRepository.getResource(new ResourceLocation(stoneModel.getNamespace(), "models/" + stoneModel.getPath() + ".json"));
                    } catch (IOException e) {
                        read = BackupFetcher.provideBlockModelFile(stoneModel);
                    }
                    BlockModelParser parentModel = GSON.fromJson(new BufferedReader(new InputStreamReader(read, StandardCharsets.UTF_8)), BlockModelParser.class);
                    List<ResourceLocation> stoneLocs = parentModel.textures.values().stream().map(x->ResourceLocation.of(x,':')).toList();


                    int i2 = 0;
                    for (ResourceLocation m : defaultModels) {
                        InputStream stoneRead;
                        try {
                            stoneRead = ClientPrePackRepository.getResource(new ResourceLocation(stoneModel.getNamespace(), "models/" + stoneModel.getPath() + ".json"));
                        } catch (IOException e) {
                            stoneRead = BackupFetcher.provideBlockModelFile(stoneModel);
                        }
                        BlockModelParser outputModel = GSON.fromJson(new BufferedReader(new InputStreamReader(stoneRead, StandardCharsets.UTF_8)), BlockModelParser.class);
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
                        BlockModelParser oreModel = GSON.fromJson(oreTextBuilder.toString(), BlockModelParser.class);
                        String maybeTex = oreModel.textures.values().stream().filter(x ->
                                        !stoneLocs.contains(ResourceLocation.of(x, ':')) && newTexMap.keySet().stream().anyMatch(y->{
                                            var rl = ResourceLocation.of(x,':');
                                            return y.last().equals(rl);
                                        }))
                                .findFirst().orElse(null);
                        ResourceLocation mainOreTex = maybeTex==null?null:ResourceLocation.of(maybeTex, ':');
                        var overlayTextures = oreModel.textures.values().stream().filter(x->
                                newTexMap.entrySet().stream().anyMatch(e->{
                                    var y = e.getKey();
                                    var rl = ResourceLocation.of(x,':');
                                    return y.last().equals(rl)&&y.last().equals(e.getValue());
                                })).toList();
                        for (String s : outputModel.textures.keySet()) {
                            String val = outputModel.textures.get(s);
                            ResourceLocation old = ResourceLocation.of(val,':');
                            ResourceLocation lookup = newTexMap.get(new Pair<>(old,mainOreTex));
                            if (lookup!=null) outputModel.replaceTexture(old,lookup);
                        }
                        int i = 0;
                        for (String s : overlayTextures) {
                            var rl = ResourceLocation.of(s,':');
                            outputModel.addOverlay(i,rl);
                            i++;
                        }
                        outputMap.add("textures",GSON.toJsonTree(outputModel.textures));
                        if (outputModel.elements!=null&&outputModel.elements.size()!=0) outputMap.add("elements",outputModel.elements);

                        var outModelRL = new ResourceLocation(ExcavatedVariants.MOD_ID, "block/"+full_id + i2);
                        outModelRLs.add(outModelRL);
                        String finalJson = GSON.toJson(outputMap);
                        resources.put(new ResourceLocation(ExcavatedVariants.MOD_ID,"models/"+outModelRL.getPath()+".json"),
                                ()->new ByteArrayInputStream(finalJson.getBytes()));
                        i2++;
                    }

                    // Create output BS
                    var assembler = BlockstateModelParser.create(outBlock,outModelRLs);
                    String bs = GSON.toJson(assembler);
                    resources.put(new ResourceLocation(ExcavatedVariants.MOD_ID,"blockstates/"+full_id+".json"),
                            ()->new ByteArrayInputStream(bs.getBytes()));
                } catch (IOException e) {
                    //TODO: add stuff here
                    ExcavatedVariants.LOGGER.error(e);
                    //throw new RuntimeException(e);
                }
            } else {
                ExcavatedVariants.LOGGER.warn("Missing {}for ore {}", ""+
                        (oreInfo==null?"ore model info, ":"")+
                        (stoneInfo==null?"stone model info, ":"")+
                        (extractorPair==null?"texture extractor info, ":"")+
                        (outBlock==null?"registered block, ":""),full_id);
            }
        }

        return resources;
    }

    private static Pair<BlockModelDefinition,List<ResourceLocation>> getInfoFromBlockstate(ResourceLocation oreRl, BlockModelDefinition.Context ctx) throws IOException {
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

            List<ResourceLocation> oreTextures = new ArrayList<>();
            for (ResourceLocation mRl : oreModels) {
                ResourceLocation actual = new ResourceLocation(mRl.getNamespace(), "models/" + mRl.getPath() + ".json");
                InputStream is;
                try {
                    is = ClientPrePackRepository.getResource(actual);
                } catch (IOException e) {
                    is = BackupFetcher.provideBlockModelFile(mRl);
                }
                BlockModelParser map = GSON.fromJson(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)), BlockModelParser.class);
                if (map.textures != null) {
                    for (String i : map.textures.values()) {
                        ResourceLocation o = ResourceLocation.of(i, ':');
                        oreTextures.add(new ResourceLocation(o.getNamespace(), "textures/" + o.getPath() + ".png"));
                    }
                }
            }
            return new Pair<>(oreBMD, oreTextures);
        }
        return null;
    }
}
