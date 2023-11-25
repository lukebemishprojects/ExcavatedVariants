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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A stone is a block which ores generate in.
 */
public final class Stone {
    public static final Codec<Stone> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translations").forGetter(s -> s.translations),
            ResourceKey.codec(Registries.BLOCK).fieldOf("block").forGetter(s -> s.block),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types),
            ResourceLocation.CODEC.listOf().fieldOf("ore_tags").forGetter(o -> o.oreTags)
    ).apply(i, Stone::new));

    public final Map<String, String> translations;
    public final ResourceKey<Block> block;
    public final Set<ResourceKey<GroundType>> types;
    public final List<ResourceLocation> oreTags;

    private Stone(Map<String, String> translations, ResourceKey<Block> block, Set<ResourceKey<GroundType>> types, List<ResourceLocation> oreTags) {
        this.translations = translations;
        this.block = block;
        this.types = types;
        this.oreTags = oreTags;
    }

    public Holder<Stone> getHolder() {
        return RegistriesImpl.STONE_REGISTRY.wrapAsHolder(this);
    }

    public ResourceKey<Stone> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered stone"));
    }

    public static class Builder {
        private Map<String, String> translations;
        private ResourceKey<Block> block;
        private Set<ResourceKey<GroundType>> types;
        private List<ResourceLocation> oreTags;

        /**
         * @param translations a map from language codes (e.g. {@code "en_us"}) to translated names for this stone
         */
        public Builder setTranslations(Map<String, String> translations) {
            this.translations = translations;
            return this;
        }

        /**
         * @param block the block which this stone represents
         */
        public Builder setBlock(ResourceKey<Block> block) {
            this.block = block;
            return this;
        }

        /**
         * @param types the types of ground which this stone can generate ores in
         */
        public Builder setTypes(Set<ResourceKey<GroundType>> types) {
            this.types = types;
            return this;
        }

        /**
         * @param oreTags tags which should be added to ores which generate in this stone
         */
        public Builder setOreTags(List<ResourceLocation> oreTags) {
            this.oreTags = oreTags;
            return this;
        }

        public Stone build() {
            Objects.requireNonNull(block);
            Objects.requireNonNull(translations);
            Objects.requireNonNull(types);
            Objects.requireNonNull(oreTags);
            return new Stone(translations, block, types, oreTags);
        }
    }

    public TagKey<Block> getOreTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_in_stone/"));
    }
}
