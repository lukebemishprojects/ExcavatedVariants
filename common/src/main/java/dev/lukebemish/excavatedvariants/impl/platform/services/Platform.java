/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.platform.services;

import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface Platform {

    Path getConfigFolder();
    Path getModDataFolder();

    String getModVersion();

    boolean isClient();
    Set<String> getModIds();

    /**
     * Gets a list of mining levels, ordered softest to hardest
     */
    List<ResourceLocation> getMiningLevels(ResourceGenerationContext context);
    void register(ExcavatedVariants.VariantFuture future);
}
