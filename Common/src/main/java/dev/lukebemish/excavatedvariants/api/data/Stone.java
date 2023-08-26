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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Stone {
    public static final Codec<Stone> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translations").forGetter(s -> s.translations),
            ResourceKey.codec(Registries.BLOCK).fieldOf("block").forGetter(s -> s.block),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types)
    ).apply(i, Stone::new));

    public final Map<String, String> translations;
    public final ResourceKey<Block> block;
    public final Set<ResourceKey<GroundType>> types;

    private Stone(Map<String, String> translations, ResourceKey<Block> block, Set<ResourceKey<GroundType>> types) {
        this.translations = translations;
        this.block = block;
        this.types = types;
    }

    public final Holder<Stone> getHolder() {
        return RegistriesImpl.STONE_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<Stone> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered stone"));
    }

    public static class Builder {
        private Map<String, String> translations;
        private ResourceKey<Block> block;
        private Set<ResourceKey<GroundType>> types;

        public Builder setTranslations(Map<String, String> translations) {
            this.translations = translations;
            return this;
        }

        public Builder setBlock(ResourceKey<Block> block) {
            this.block = block;
            return this;
        }

        public Builder setTypes(Set<ResourceKey<GroundType>> types) {
            this.types = types;
            return this;
        }

        public Stone build() {
            Objects.requireNonNull(block);
            Objects.requireNonNull(translations);
            Objects.requireNonNull(types);
            return new Stone(translations, block, types);
        }
    }

    public final TagKey<Block> getOreTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_in_stone/"));
    }
}
