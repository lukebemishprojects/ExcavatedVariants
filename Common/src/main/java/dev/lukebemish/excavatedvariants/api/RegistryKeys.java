/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api;

import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class RegistryKeys {
    private RegistryKeys() {}

    /**
     * Ground types represent classes of ores and stones. They are used to determine which new ore/stone combinations
     * (variants) should be created.
     */
    public static final ResourceKey<Registry<GroundType>> GROUND_TYPE = RegistriesImpl.GROUND_TYPE_KEY;
    /**
     * Ores are types of blocks which can exist in any number of different stones. During worldgen, they are replaced
     * with variants matching neighboring stones.
     */
    public static final ResourceKey<Registry<Ore>> ORE = RegistriesImpl.ORE_KEY;
    /**
     * Stones are blocks which ores generate within.
     */
    public static final ResourceKey<Registry<Stone>> STONE = RegistriesImpl.STONE_KEY;
    /**
     * Modifiers pre- or post-process ore/stone combinations to either prevent or modify variant creation, or change the
     * properties of the resulting variants.
     */
    public static final ResourceKey<Registry<Modifier>> MODIFIER = RegistriesImpl.MODIFIER_KEY;
}
