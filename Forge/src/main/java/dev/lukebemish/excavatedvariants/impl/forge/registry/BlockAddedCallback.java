/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge.registry;

import com.google.common.base.Suppliers;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.forge.ExcavatedVariantsForge;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlockAddedCallback {
    private static final Supplier<ModContainer> EV_CONTAINER = Suppliers.memoize(() -> ModList.get().getModContainerById(ExcavatedVariants.MOD_ID).orElseThrow());

    private static boolean isRegistering = false;

    public static void register() {
        if (ExcavatedVariants.hasLoaded() && !isRegistering) {
            isRegistering = true;
            ExcavatedVariants.RegistryFuture future;
            while ((future = ExcavatedVariants.READY_QUEUE.poll()) != null) {
                ExcavatedVariants.registerBlockAndItem((rlr, bl) -> {
                    final ModContainer activeContainer = ModLoadingContext.get().getActiveContainer();
                    ModLoadingContext.get().setActiveContainer(EV_CONTAINER.get());
                    ForgeRegistries.BLOCKS.register(rlr, bl);
                    ModLoadingContext.get().setActiveContainer(activeContainer);
                }, (rlr, it) -> {
                    ExcavatedVariantsForge.TO_REGISTER.register(rlr.getPath(), it);
                    return () -> Services.REGISTRY_UTIL.getItemById(rlr);
                }, future);
            }
            isRegistering = false;
        }
    }
}
