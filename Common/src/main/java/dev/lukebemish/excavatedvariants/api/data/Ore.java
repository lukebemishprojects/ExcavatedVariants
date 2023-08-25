/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.*;

public class Ore {
    public static final Codec<Ore> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.listOf().optionalFieldOf("names", List.of()).forGetter(o -> o.names),
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceKey.codec(RegistryKeys.STONE)).fieldOf("blocks").forGetter(o -> o.blocks),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translation").forGetter(o -> o.translation),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types)
    ).apply(i, Ore::new));

    public final List<String> names;
    private final Map<ResourceLocation, ResourceKey<Stone>> blocks;
    private Map<ResourceKey<Block>, ResourceKey<Stone>> blocksBaked;
    public final Map<String, String> translation;
    public final Set<ResourceKey<GroundType>> types;

    private Ore(List<String> names, Map<ResourceLocation, ResourceKey<Stone>> blocks, Map<String, String> translation, Set<ResourceKey<GroundType>> types) {
        this.names = names;
        this.blocks = new HashMap<>(blocks);
        this.translation = translation;
        this.types = types;

        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new HashMap<>();
        for (Map.Entry<ResourceLocation, ResourceKey<Stone>> entry : blocks.entrySet()) {
            ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
            baked.put(block, entry.getValue());
        }
        this.blocksBaked = Collections.unmodifiableMap(baked);
    }

    public final Map<ResourceKey<Block>, ResourceKey<Stone>> getBlocks() {
        return blocksBaked;
    }

    @ApiStatus.Internal
    public final void addPossibleVariant(Stone stone, ResourceLocation output) {
        blocks.put(output, stone.getKeyOrThrow());
    }

    @ApiStatus.Internal
    public synchronized final void bakeExistingBlocks() {
        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new HashMap<>();
        for (Map.Entry<ResourceLocation, ResourceKey<Stone>> entry : blocks.entrySet()) {
            if (BuiltInRegistries.BLOCK.containsKey(entry.getKey())) {
                ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
                baked.put(block, entry.getValue());
            }
        }
        blocksBaked = Collections.unmodifiableMap(baked);
    }

    @Contract(value = "_ -> new", pure = true)
    public static Ore merge(List<Ore> ores) {
        List<String> alternativeNames = new ArrayList<>();
        Set<String> alternativeNamesSet = new HashSet<>();
        Map<ResourceLocation, ResourceKey<Stone>> blocks = new HashMap<>();
        Map<String, String> translation = new HashMap<>();
        Set<ResourceKey<GroundType>> types = new HashSet<>();
        for (Ore ore : ores) {
            for (String alternativeName : ore.names) {
                if (!alternativeNamesSet.contains(alternativeName)) {
                    alternativeNamesSet.add(alternativeName);
                    alternativeNames.add(alternativeName);
                }
            }
            blocks.putAll(ore.blocks);
            translation.putAll(ore.translation);
            types.addAll(ore.types);
        }
        return new Ore(alternativeNames, blocks, translation, types);
    }

    public final Holder<Ore> getHolder() {
        return RegistriesImpl.ORE_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<Ore> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ore"));
    }

    public static class Builder {
        private List<String> names;
        private Map<ResourceLocation, ResourceKey<Stone>> blocks;
        private Map<String, String> translation;
        private Set<ResourceKey<GroundType>> types;

        public Builder setNames(List<String> names) {
            this.names = names;
            return this;
        }

        public Builder setBlocks(Map<ResourceLocation, ResourceKey<Stone>> blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder setTranslation(Map<String, String> translation) {
            this.translation = translation;
            return this;
        }

        public Builder setTypes(Set<ResourceKey<GroundType>> types) {
            this.types = types;
            return this;
        }

        public Ore build() {
            Objects.requireNonNull(names);
            Objects.requireNonNull(blocks);
            Objects.requireNonNull(translation);
            Objects.requireNonNull(types);
            return new Ore(names, blocks, translation, types);
        }
    }

    public final TagKey<Block> getTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores/"));
    }

    public final TagKey<Block> getConvertibleTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_convertible/"));
    }
}
