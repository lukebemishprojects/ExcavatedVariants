/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.modifier;

import net.minecraft.util.valueproviders.IntProvider;

import java.util.function.Consumer;

/**
 * A modifier which can be applied to a block's properties.
 */
public interface BlockPropsModifier {
    /**
     * Optionally, change the block's destroy time.
     * @param consumer accepts a new destroy time
     */
    default void setDestroyTime(Consumer<Float> consumer) {}

    /**
     * Optionally, change the block's explosion resistance.
     * @param consumer accepts a new explosion resistance
     */
    default void setExplosionResistance(Consumer<Float> consumer) {}

    /**
     * Optionally, change how the block drops experience
     * @param consumer accepts a new experience provider
     */
    default void setXpDropped(Consumer<IntProvider> consumer) {}
}
