/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric;

import dev.lukebemish.excavatedvariants.impl.fabriquilt.ExcavatedVariantsFabriQuilt;
import net.fabricmc.api.ModInitializer;

public class ExcavatedVariantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExcavatedVariantsFabriQuilt.onInitialize();
    }
}
