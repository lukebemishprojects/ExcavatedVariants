/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.platform.services;

import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface Platform {
    boolean isQuilt();

    boolean isForge();

    Collection<String> getModIds();

    Path getConfigFolder();

    Path getModDataFolder();

    boolean isClient();

    /**
     * Gets a list of mining levels, ordered softest to hardest
     */
    List<ResourceLocation> getMiningLevels(ResourceGenerationContext context, Consumer<String> cacheKeyConsumer);
    void register(ExcavatedVariants.VariantFuture future);
}
