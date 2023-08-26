/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;

public final class GroundType {
    public static final Codec<GroundType> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimensions").forGetter(g -> g.dimensions)
    ).apply(i, GroundType::new));

    public final List<ResourceKey<Level>> dimensions;

    private GroundType(List<ResourceKey<Level>> dimensions) {
        this.dimensions = dimensions;
    }

    public Holder<GroundType> getHolder() {
        return RegistriesImpl.GROUND_TYPE_REGISTRY.wrapAsHolder(this);
    }

    public ResourceKey<GroundType> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ground type"));
    }

    public static class Builder {
        private List<ResourceKey<Level>> dimensions;

        public Builder setDimensions(List<ResourceKey<Level>> dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public GroundType build() {
            Objects.requireNonNull(dimensions);
            return new GroundType(dimensions);
        }
    }

    public TagKey<Block> getOreTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_in_ground_type/"));
    }

    public TagKey<Block> getStoneTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("stones_in_ground_type/"));
    }
}
