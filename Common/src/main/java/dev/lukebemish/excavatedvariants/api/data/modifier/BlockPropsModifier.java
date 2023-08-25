/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.modifier;

import net.minecraft.util.valueproviders.IntProvider;

import java.util.function.Consumer;

public interface BlockPropsModifier {
    default void setDestroyTime(Consumer<Float> consumer) {}
    default void setExplosionResistance(Consumer<Float> consumer) {}
    default void setXpDropped(Consumer<IntProvider> consumer) {}
}
