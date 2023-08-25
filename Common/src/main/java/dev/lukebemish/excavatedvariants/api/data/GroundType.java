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
import net.minecraft.world.level.Level;

import java.util.Objects;

public class GroundType {
    public static final Codec<GroundType> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension_tag").forGetter(g -> g.dimensionTag)
    ).apply(i, GroundType::new));

    public final ResourceKey<Level> dimensionTag;

    private GroundType(ResourceKey<Level> dimensionTag) {
        this.dimensionTag = dimensionTag;
    }

    public final Holder<GroundType> getHolder() {
        return RegistriesImpl.GROUND_TYPE_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<GroundType> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ground type"));
    }

    public static class Builder {
        private ResourceKey<Level> dimensionTag;

        public Builder setDimensionTag(ResourceKey<Level> dimensionTag) {
            this.dimensionTag = dimensionTag;
            return this;
        }

        public GroundType build() {
            Objects.requireNonNull(dimensionTag);
            return new GroundType(dimensionTag);
        }
    }
}
