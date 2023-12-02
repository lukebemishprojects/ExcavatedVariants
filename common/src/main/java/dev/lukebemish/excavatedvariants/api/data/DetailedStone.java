/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public final class DetailedStone {
    private static final Codec<PartialDetailedStone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(RegistryKeys.STONE).fieldOf("stone").forGetter(PartialDetailedStone::stone),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(PartialDetailedStone::requiredMods),
            Codec.BOOL.optionalFieldOf("generating", false).forGetter(PartialDetailedStone::generating)
    ).apply(instance, PartialDetailedStone::new));
    private record PartialDetailedStone(ResourceKey<Stone> stone, Optional<List<String>> requiredMods, boolean generating) {
        public DetailedStone toDetailedStone(ResourceLocation key) {
            return new DetailedStone(stone, requiredMods.orElse(List.of(key.getNamespace())), generating);
        }
    }
    private static final Codec<Either<ResourceKey<Stone>, PartialDetailedStone>> EITHER_CODEC = Codec.either(ResourceKey.codec(RegistryKeys.STONE), CODEC);
    public static final Codec<Map<ResourceLocation, DetailedStone>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, EITHER_CODEC)
            .xmap(
                    map ->
                            map.entrySet().stream()
                                    .map(e -> new Pair<>(e.getKey(), e.getValue().map(k -> new DetailedStone(k, List.of(e.getKey().getNamespace()), false), partial -> partial.toDetailedStone(e.getKey()))))
                                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                    map ->
                            map.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, m -> {
                                        if (m.getValue().modIds.size() == 1 && m.getValue().modIds.get(0).equals(m.getKey().getNamespace()) && !m.getValue().isGenerating()) {
                                            return Either.left(m.getValue().getStone());
                                        } else {
                                            return Either.right(new PartialDetailedStone(
                                                    m.getValue().getStone(),
                                                    Optional.of(m.getValue().modIds),
                                                    m.getValue().isGenerating()
                                            ));
                                        }
                                    }))
            ).flatXmap(map -> {
                if (map.values().stream().map(s -> Set.copyOf(s.requiredMods())).collect(Collectors.toSet()).size() == 1 && map.values().stream().noneMatch(DetailedStone::isGenerating)) {
                    HashMap<ResourceLocation, DetailedStone> newMap = new HashMap<>();
                    for (Map.Entry<ResourceLocation, DetailedStone> entry : map.entrySet()) {
                        DetailedStone detailedStone = entry.getValue();
                        DetailedStone newStone = new DetailedStone.Builder().setModIds(detailedStone.requiredMods()).setStone(detailedStone.getStone()).setGenerating(true).build();
                        newMap.put(entry.getKey(), newStone);
                    }
                    return DataResult.success(newMap);
                } else if (map.values().stream().filter(DetailedStone::isGenerating).map(s -> Set.copyOf(s.requiredMods())).collect(Collectors.toSet()).size() == 1) {
                    return DataResult.success(map);
                } else {
                    return DataResult.error(() -> "A generating ore block must be present, and all generating ore blocks (or candidates for implicit detection) must require the same mods");
                }
            }, DataResult::success);
    private final ResourceKey<Stone> stone;
    private final List<String> modIds;
    private final boolean generating;

    private DetailedStone(ResourceKey<Stone> stone, List<String> modIds, boolean generating) {
        this.stone = stone;
        this.modIds = modIds;
        this.generating = generating;
    }

    public List<String> requiredMods() {
        return this.modIds;
    }

    public boolean hasRequiredMods() {
        return Services.PLATFORM.getModIds().containsAll(this.modIds);
    }

    public ResourceKey<Stone> getStone() {
        return stone;
    }

    public boolean isGenerating() {
        return generating;
    }

    public static class Builder {
        private ResourceKey<Stone> stone;
        private List<String> modIds;
        private boolean generating = false;

        /**
         * @param stone the represented stone of this variant/stone pairing
         */

        public Builder setStone(ResourceKey<Stone> stone) {
            this.stone = stone;
            return this;
        }

        /**
         * @param generating whether this variant/stone pairing can be used to parent new, generated variants
         */
        public Builder setGenerating(boolean generating) {
            this.generating = generating;
            return this;
        }

        /**
         * @param modIds mods that must be present for the represented variant/stone pairing to be recognized
         */
        public Builder setModIds(List<String> modIds) {
            this.modIds = modIds;
            return this;
        }

        public DetailedStone build() {
            Objects.requireNonNull(stone);
            Objects.requireNonNull(modIds);
            return new DetailedStone(stone, modIds, generating);
        }

        public static Builder of(DetailedStone key) {
            return new Builder()
                    .setStone(key.getStone())
                    .setGenerating(key.isGenerating())
                    .setModIds(key.modIds);
        }
    }
}
