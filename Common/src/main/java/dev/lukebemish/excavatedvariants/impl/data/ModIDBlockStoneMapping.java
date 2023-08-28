/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ModIDBlockStoneMapping(ResourceKey<Stone> stone, List<String> modIds) {
    private static final Codec<ModIDBlockStoneMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(RegistryKeys.STONE).fieldOf("stone").forGetter(ModIDBlockStoneMapping::stone),
            Codec.STRING.listOf().fieldOf("required_mods").forGetter(ModIDBlockStoneMapping::modIds)
    ).apply(instance, ModIDBlockStoneMapping::new));
    private static final Codec<Either<ResourceKey<Stone>, ModIDBlockStoneMapping>> EITHER_CODEC = Codec.either(ResourceKey.codec(RegistryKeys.STONE), CODEC);
    public static final Codec<Map<ResourceLocation, ResourceKey<Stone>>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, EITHER_CODEC)
            .xmap(
                    map ->
                            map.entrySet().stream()
                                    .map(e -> new Pair<>(e.getKey(), e.getValue().map(k -> new ModIDBlockStoneMapping(k, List.of(e.getKey().getNamespace())), Function.identity())))
                                    .filter(e -> e.getSecond().hasRequiredMods())
                                    .collect(Collectors.toMap(Pair::getFirst, e -> e.getSecond().stone())),
                    map ->
                            map.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> Either.left(e.getValue())))
            );

    public boolean hasRequiredMods() {
        return Services.PLATFORM.getModIds().containsAll(this.modIds());
    }
}
