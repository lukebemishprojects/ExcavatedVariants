/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DetailedStone {
    private static final Codec<DetailedStone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(RegistryKeys.STONE).fieldOf("stone").forGetter(DetailedStone::getStone),
            Codec.STRING.listOf().fieldOf("required_mods").forGetter(s -> s.modIds),
            Codec.BOOL.optionalFieldOf("generating", true).forGetter(DetailedStone::isGenerating)
    ).apply(instance, DetailedStone::new));
    private static final Codec<Either<ResourceKey<Stone>, DetailedStone>> EITHER_CODEC = Codec.either(ResourceKey.codec(RegistryKeys.STONE), CODEC);
    public static final Codec<Map<ResourceLocation, DetailedStone>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, EITHER_CODEC)
            .xmap(
                    map ->
                            map.entrySet().stream()
                                    .map(e -> new Pair<>(e.getKey(), e.getValue().map(k -> new DetailedStone(k, List.of(e.getKey().getNamespace()), true), Function.identity())))
                                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
                    map ->
                            map.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, m -> {
                                        if (m.getValue().modIds.size() == 1 && m.getValue().modIds.get(0).equals(m.getKey().getNamespace()) && m.getValue().isGenerating()) {
                                            return Either.left(m.getValue().getStone());
                                        } else {
                                            return Either.right(m.getValue());
                                        }
                                    }))
            );
    private final ResourceKey<Stone> stone;
    private final List<String> modIds;
    private final boolean generating;

    private DetailedStone(ResourceKey<Stone> stone, List<String> modIds, boolean generating) {
        this.stone = stone;
        this.modIds = modIds;
        this.generating = generating;
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
        private boolean generating = true;

        public Builder setStone(ResourceKey<Stone> stone) {
            this.stone = stone;
            return this;
        }

        public Builder setGenerating(boolean generating) {
            this.generating = generating;
            return this;
        }

        public Builder setModIds(List<String> modIds) {
            this.modIds = modIds;
            return this;
        }

        public DetailedStone build() {
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
