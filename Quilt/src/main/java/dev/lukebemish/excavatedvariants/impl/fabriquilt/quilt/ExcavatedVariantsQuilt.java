/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt;

import dev.lukebemish.excavatedvariants.impl.BlockAddedCallback;
import dev.lukebemish.excavatedvariants.impl.fabriquilt.ExcavatedVariantsFabriQuilt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

public class ExcavatedVariantsQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer modContainer) {
        ExcavatedVariantsFabriQuilt.onInitialize();

        RegistryEvents.getEntryAddEvent(BuiltInRegistries.BLOCK).register(ctx ->
                BlockAddedCallback.onRegister(ctx.value(), ResourceKey.create(Registries.BLOCK, ctx.id())));
    }
}
