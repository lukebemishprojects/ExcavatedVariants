package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.dynamic_asset_generator.Pair;
import com.github.lukebemish.dynamic_asset_generator.api.ResettingSupplier;
import com.github.lukebemish.dynamic_asset_generator.api.ServerPrePackRepository;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

public class MiningLevelTagGenerator {
    private final String level;
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();

    private record CheckPair(String full_id, String base_id) { }
    HashSet<String> to_add;

    public MiningLevelTagGenerator(String level) {
        this.level = level;
    }

    public void add(String full_id, BaseOre ore) {
        toCheck.add(new CheckPair(full_id, ore.block_id.get(0).toString()));
    }
    public List<Pair<ResourceLocation,Supplier<Boolean>>> suppliers() {
        List<Pair<ResourceLocation, Supplier<Boolean>>> outList = new ArrayList<>();
        for (CheckPair c : toCheck) {
            outList.add(new Pair<>(new ResourceLocation(ExcavatedVariants.MOD_ID,c.full_id()),supplyFor(c.full_id())));
        }
        return outList;
    }
    public ResettingSupplier<Boolean> supplyFor(String full_id) {
        return new ResettingSupplier<Boolean>() {
            @Override
            public void reset() {
                MiningLevelTagGenerator.this.reset();
            }

            @Override
            public Boolean get() {
                MiningLevelTagGenerator.this.get();
                return to_add.contains(full_id);
            }
        };
    }

    public void get() {
        if (to_add==null) {
            to_add = new HashSet<>();
            try {
                List<InputStream> read = ServerPrePackRepository.getResources(new ResourceLocation("minecraft", "tags/blocks/needs_" + level + "_tool.json"));
                for (InputStream is : read) {
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader
                            (is, Charset.forName(StandardCharsets.UTF_8.name())));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                    String readStr = textBuilder.toString();
                    JsonElement parser = JsonParser.parseString(readStr);
                    if (parser.isJsonObject()) {
                        JsonElement replace = parser.getAsJsonObject().get("replace");
                        if (!(replace == null) && replace.isJsonPrimitive() && replace.getAsJsonPrimitive().isBoolean() && replace.getAsBoolean()) {
                            to_add.clear();
                        }
                        JsonElement values = parser.getAsJsonObject().get("values");
                        if (values.isJsonArray()) {
                            for (JsonElement i : values.getAsJsonArray()) {
                                if (i.isJsonPrimitive()) {
                                    if (i.getAsJsonPrimitive().isString()) {
                                        String str = i.getAsJsonPrimitive().getAsString();
                                        for (CheckPair j : toCheck) {
                                            if (j.base_id.equals(str)) {
                                                to_add.add(j.full_id);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is.close();
                }
            } catch (IOException e) {
                ExcavatedVariants.LOGGER.error("Could not load mining level tag for {}; will be empty...\n{}", level, e);
            }
        }
    }

    public void reset() {
        to_add = null;
    }
}
