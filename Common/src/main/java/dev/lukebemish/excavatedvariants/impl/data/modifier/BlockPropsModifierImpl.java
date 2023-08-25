/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.data.modifier.BlockPropsModifier;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Optional;
import java.util.function.Consumer;

public record BlockPropsModifierImpl(Optional<Float> destroyTime, Optional<Float> explosionResistance,
                                     Optional<IntProvider> xpDropped) implements BlockPropsModifier {
    public static final Codec<BlockPropsModifierImpl> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("destroy_time").forGetter(BlockPropsModifierImpl::destroyTime),
            Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(BlockPropsModifierImpl::explosionResistance),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("xp").forGetter(BlockPropsModifierImpl::xpDropped)
    ).apply(i, BlockPropsModifierImpl::new));

    @Override
    public void setDestroyTime(Consumer<Float> consumer) {
        destroyTime.ifPresent(consumer);
    }

    @Override
    public void setExplosionResistance(Consumer<Float> consumer) {
        explosionResistance.ifPresent(consumer);
    }

    @Override
    public void setXpDropped(Consumer<IntProvider> consumer) {
        xpDropped.ifPresent(consumer);
    }
}
