/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.platform.services;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;

public interface MainPlatformTarget {

    void registerFeatures();

    ModifiedOreBlock makeDefaultOreBlock(ExcavatedVariants.VariantFuture future);
}
