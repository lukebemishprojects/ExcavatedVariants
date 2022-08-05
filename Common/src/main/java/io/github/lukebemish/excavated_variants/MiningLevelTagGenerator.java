package io.github.lukebemish.excavated_variants;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.lukebemish.dynamic_asset_generator.api.ServerPrePackRepository;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MiningLevelTagGenerator implements Supplier<Set<ResourceLocation>> {
    private final String level;
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();

    public MiningLevelTagGenerator(String level) {
        this.level = level;
    }

    public void add(String fullId, BaseOre ore) {
        toCheck.add(new CheckPair(fullId, ore.block_id.get(0).toString()));
    }

    public Set<ResourceLocation> get() {
        Set<ResourceLocation> toAdd = new HashSet<>();
        try {
            List<InputStream> read = ServerPrePackRepository.getResources(new ResourceLocation("minecraft", "tags/blocks/needs_" + level + "_tool.json")).toList();
            try {
                for (InputStream is : read) {
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader
                            (is, StandardCharsets.UTF_8));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                    String readStr = textBuilder.toString();
                    JsonElement parser = JsonParser.parseString(readStr);
                    if (parser.isJsonObject()) {
                        JsonElement replace = parser.getAsJsonObject().get("replace");
                        if (replace != null && replace.isJsonPrimitive() && replace.getAsJsonPrimitive().isBoolean() && replace.getAsBoolean()) {
                            toAdd.clear();
                        }
                        JsonElement values = parser.getAsJsonObject().get("values");
                        List<String> entries = new ArrayList<>();
                        if (values.isJsonArray()) {
                            for (JsonElement i : values.getAsJsonArray()) {
                                if (i.isJsonPrimitive()) {
                                    if (i.getAsJsonPrimitive().isString()) {
                                        String str = i.getAsJsonPrimitive().getAsString();
                                        entries.addAll(parseTagEntry(str));
                                    }
                                }
                            }
                        }
                        for (String str : entries) {
                            for (CheckPair j : toCheck) {
                                if (j.base_id.equals(str) && Registry.ITEM.containsKey(new ResourceLocation(ExcavatedVariants.MOD_ID, j.full_id))) {
                                    toAdd.add(new ResourceLocation(ExcavatedVariants.MOD_ID, j.full_id));
                                }
                            }
                        }
                    }
                    is.close();
                }
            } catch (IOException e) {
                ExcavatedVariants.LOGGER.error("Could not load mining level tag for {}; will be empty...\n{}", level, e);
            } finally {
                read.forEach(is -> {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Could not load mining level tag for {}; will be empty...\n{}", level, e);
        }
        return toAdd;
    }

    public List<String> parseTagEntry(String tagEntry) {
        if (!tagEntry.startsWith("#")) return List.of(tagEntry);
        List<String> entries = new ArrayList<>();
        try {
            List<InputStream> read = ServerPrePackRepository.getResources(new ResourceLocation("minecraft", "tags/blocks/needs_" + level + "_tool.json")).toList();
            try {
                for (InputStream is : read) {
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader
                            (is, StandardCharsets.UTF_8));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                    String readStr = textBuilder.toString();
                    JsonElement parser = JsonParser.parseString(readStr);
                    if (parser.isJsonObject()) {
                        JsonElement replace = parser.getAsJsonObject().get("replace");
                        if (replace != null && replace.isJsonPrimitive() && replace.getAsJsonPrimitive().isBoolean() && replace.getAsBoolean()) {
                            entries.clear();
                        }
                        JsonElement values = parser.getAsJsonObject().get("values");
                        if (values.isJsonArray()) {
                            for (JsonElement i : values.getAsJsonArray()) {
                                if (i.isJsonPrimitive()) {
                                    if (i.getAsJsonPrimitive().isString()) {
                                        String str = i.getAsJsonPrimitive().getAsString();
                                        entries.add(str);
                                    }
                                }
                            }
                        }
                    }
                    is.close();
                }
                return entries;
            } catch (IOException e) {
                ExcavatedVariants.LOGGER.error("Could not load tag {}; ignoring...", tagEntry);
            } finally {
                read.forEach(is -> {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.warn("Could not load tag {}; ignoring...", tagEntry);
        }
        return List.of();
    }

    private record CheckPair(String full_id, String base_id) {
    }
}
