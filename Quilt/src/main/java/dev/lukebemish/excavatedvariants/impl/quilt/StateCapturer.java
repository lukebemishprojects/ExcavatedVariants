/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModLifecycle;
import net.fabricmc.api.ModInitializer;

@SuppressWarnings("deprecation")
public class StateCapturer implements ModInitializer {
    private static boolean INITIALIZED = false;

    @Override
    public void onInitialize() {
        INITIALIZED = true;
    }

    public static void checkState() {
        if (ModLifecycle.getLifecyclePhase() == ModLifecycle.PRE || !INITIALIZED) {
            var e = new RuntimeException("Something has gone very wrong with load ordering, and we have no clue what is going on. Please report this to Excavated Variants and be sure to provide a log!");
            ExcavatedVariants.LOGGER.error("...what the heck? Where are we? Lifecycle state: {}, initialized: {}", ModLifecycle.getLifecyclePhase(), INITIALIZED, e);
            throw e;
        }
    }
}
