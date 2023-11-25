/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.serialization.Codec;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class GroundType {
    public static final Codec<GroundType> CODEC = Codec.unit(GroundType::new);

    private GroundType() {}

    public Holder<GroundType> getHolder() {
        return RegistriesImpl.GROUND_TYPE_REGISTRY.wrapAsHolder(this);
    }

    public ResourceKey<GroundType> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ground type"));
    }

    public static class Builder {
        // More data may be stored here in the future

        public GroundType build() {
            return new GroundType();
        }
    }

    public TagKey<Block> getOreTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_in_ground_type/"));
    }

    public TagKey<Block> getStoneTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("stones_in_ground_type/"));
    }
}
