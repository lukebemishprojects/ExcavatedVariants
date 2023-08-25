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
    public static final ResourceKey<Registry<GroundType>> GROUND_TYPE = RegistriesImpl.GROUND_TYPE_KEY;
    public static final ResourceKey<Registry<Ore>> ORE = RegistriesImpl.ORE_KEY;
    public static final ResourceKey<Registry<Stone>> STONE = RegistriesImpl.STONE_KEY;
    public static final ResourceKey<Registry<Modifier>> MODIFIER = RegistriesImpl.MODIFIER_KEY;
}
