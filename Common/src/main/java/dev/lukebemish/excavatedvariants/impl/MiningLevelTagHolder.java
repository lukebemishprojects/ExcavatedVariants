/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.dynamicassetgenerator.api.ServerPrePackRepository;
import dev.lukebemish.dynamicassetgenerator.api.templates.TagFile;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiningLevelTagHolder implements Supplier<Map<ResourceLocation, Set<ResourceLocation>>> {
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();
    public void add(String fullId, BaseOre ore, BaseStone stone) {
        toCheck.add(new CheckPair(fullId, ore.blockId.get(0), stone.blockId));
    }

    @Override
    public Map<ResourceLocation, Set<ResourceLocation>> get() {
        Map<ResourceLocation, Set<ResourceLocation>> tags = new HashMap<>();

        List<ResourceLocation> tagNames = Services.PLATFORM.getMiningLevels();
        Map<ResourceLocation, Integer> blockToLevelMap = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> memberMap = tagNames.stream().collect(Collectors.toMap(Function.identity(), this::getTagMembers, (l1, l2) -> {
            List<ResourceLocation> out = new ArrayList<>(l1);
            out.addAll(l2);
            return out;
        }));

        toCheck.forEach(pair -> {
            int maxValue = Math.max(getOrCreateLevel(tagNames, memberMap, blockToLevelMap, pair.stoneId),
                    getOrCreateLevel(tagNames, memberMap, blockToLevelMap, pair.oreId));
            if (maxValue != -1)
                tags.computeIfAbsent(tagNames.get(maxValue), k->new HashSet<>()).add(new ResourceLocation(ExcavatedVariants.MOD_ID, pair.fullId));
        });

        return tags;
    }

    private record CheckPair(String fullId, ResourceLocation oreId, ResourceLocation stoneId) {
    }

    private int getOrCreateLevel(List<ResourceLocation> levels, Map<ResourceLocation, List<ResourceLocation>> memberMap, Map<ResourceLocation, Integer> map, ResourceLocation lookup) {
        return map.computeIfAbsent(lookup, key -> {
            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : memberMap.entrySet()) {
                if (entry.getValue().contains(key)) {
                    return levels.indexOf(entry.getKey());
                }
            }
            return -1;
        });
    }

    private List<ResourceLocation> getTagMembers(ResourceLocation location) {
        String type = location.getPath().split("/")[0];
        List<ResourceLocation> members = new ArrayList<>();
        var toRead = new ResourceLocation(location.getNamespace(), "tags/"+location.getPath()+".json");
        try (var read = ServerPrePackRepository.getResources(toRead)) {
            read.forEach(is -> {
                try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    var parser = JsonParser.parseReader(reader);
                    try {
                        TagFile file = TagFile.CODEC.parse(JsonOps.INSTANCE, parser).getOrThrow(false, e->{});
                        if (file.replace())
                            members.clear();
                        file.values().forEach(value ->
                                value.build(new TagEntry.Lookup<ResourceLocation>() {
                                    @Override
                                    public ResourceLocation element(ResourceLocation elementLocation) {
                                        return elementLocation;
                                    }

                                    @Override
                                    public Collection<ResourceLocation> tag(ResourceLocation tagLocation) {
                                        return getTagMembers(new ResourceLocation(tagLocation.getNamespace(), type+"/"+tagLocation.getPath()));
                                    }
                                }, members::add)
                        );
                    } catch (RuntimeException e) {
                        ExcavatedVariants.LOGGER.error("Issue parsing tag at '{}':",toRead,e);
                    }
                } catch (IOException e) {
                    ExcavatedVariants.LOGGER.error("Issue reading tag at '{}':",toRead,e);
                }
            });
        } catch (IOException ignored) {
            // Tag just doesn't exist
        }
        return members;
    }
}
