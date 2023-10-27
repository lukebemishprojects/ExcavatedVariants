/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.platform.services.MainPlatformTarget;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreReplacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

@AutoService(MainPlatformTarget.class)
public class MainPlatformTargetImpl implements MainPlatformTarget {

    @Override
    public void registerFeatures() {
        Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), new OreReplacer());
    }

    @Override
    public ModifiedOreBlock makeDefaultOreBlock(ExcavatedVariants.VariantFuture future) {
        return new ModifiedOreBlock(future);
    }
}
