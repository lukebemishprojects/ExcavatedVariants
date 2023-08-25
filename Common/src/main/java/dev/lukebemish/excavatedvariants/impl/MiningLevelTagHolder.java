/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.sources.TagSupplier;
import dev.lukebemish.dynamicassetgenerator.api.templates.TagFile;
import dev.lukebemish.excavatedvariants.impl.data.Ore;
import dev.lukebemish.excavatedvariants.impl.data.Stone;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MiningLevelTagHolder implements TagSupplier {
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();
    public void add(String fullId, Ore ore, Stone stone) {
        toCheck.add(new CheckPair(fullId, ore, stone));
    }

    @Override
    public Map<ResourceLocation, Set<ResourceLocation>> apply(ResourceGenerationContext context) {
        Map<ResourceLocation, Set<ResourceLocation>> tags = new HashMap<>();

        List<ResourceLocation> tagNames = Services.PLATFORM.getMiningLevels();
        Map<ResourceLocation, Integer> blockToLevelMap = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> memberMap = tagNames.stream().collect(Collectors.toMap(Function.identity(), name -> getTagMembers(name, context), (l1, l2) -> {
            List<ResourceLocation> out = new ArrayList<>(l1);
            out.addAll(l2);
            return out;
        }));

        toCheck.forEach(pair -> {
            int maxValue = Math.max(getOrCreateLevel(tagNames, memberMap, blockToLevelMap, pair.stone.block.location()),
                    pair.ore.getBlocks().keySet().stream().mapToInt(key -> {
                        if (BuiltInRegistries.BLOCK.containsKey(key)) {
                            return getOrCreateLevel(tagNames, memberMap, blockToLevelMap, key.location());
                        }
                        return -1;
                    }).max().orElse(-1));
            if (maxValue != -1)
                tags.computeIfAbsent(tagNames.get(maxValue), k->new HashSet<>()).add(new ResourceLocation(ExcavatedVariants.MOD_ID, pair.fullId));
        });

        return tags;
    }

    private record CheckPair(String fullId, Ore ore, Stone stone) {
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

    @Override
    public @Nullable String createSupplierCacheKey(ResourceLocation outRl, ResourceGenerationContext context) {
        // TODO: implement
        return TagSupplier.super.createSupplierCacheKey(outRl, context);
    }

    private List<ResourceLocation> getTagMembers(ResourceLocation location, ResourceGenerationContext context) {
        String type = location.getPath().split("/")[0];
        List<ResourceLocation> members = new ArrayList<>();
        var toRead = new ResourceLocation(location.getNamespace(), "tags/"+location.getPath()+".json");
        var foundResources = context.getResourceSource().getResourceStack(toRead);
        for (var ioSupplier : foundResources) {
            try (var is = ioSupplier.get();
                 var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
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
                                    return getTagMembers(new ResourceLocation(tagLocation.getNamespace(), type+"/"+tagLocation.getPath()), context);
                                }
                            }, members::add)
                    );
                } catch (RuntimeException e) {
                    ExcavatedVariants.LOGGER.error("Issue parsing tag at '{}':",toRead,e);
                }
            } catch (IOException e) {
                ExcavatedVariants.LOGGER.error("Issue reading tag at '{}':",toRead,e);
            }
        }
        return members;
    }
}
