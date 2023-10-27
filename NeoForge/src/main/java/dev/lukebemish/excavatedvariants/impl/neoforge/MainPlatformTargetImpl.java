/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.platform.services.MainPlatformTarget;

@AutoService(MainPlatformTarget.class)
public class MainPlatformTargetImpl implements MainPlatformTarget {

    public void registerFeatures() {
    }

    public ModifiedOreBlock makeDefaultOreBlock(ExcavatedVariants.VariantFuture future) {
        return new ForgeOreBlock(future);
    }
}
