/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.TierSortingRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TierSortingRegistry.class, remap = false)
public interface ForgeTierSortingRegistryAccessor {
    @Accessor("tiers")
    static BiMap<ResourceLocation, Tier> getTiers() {
        throw new AssertionError();
    }

    @Accessor("edges")
    static Multimap<ResourceLocation, ResourceLocation> getEdges() {
        throw new AssertionError();
    }
}
