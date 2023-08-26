/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public final class MissingVariantHelper {
    private MissingVariantHelper() {
    }

    public static ResourceLocation getBaseBlock(String fullId) {
        if (ExcavatedVariants.setupMap()) {
            Optional<ResourceLocation> firstOre = ExcavatedVariants.getMappingsCache().oreMappings.entrySet().stream()
                    .filter(it -> fullId.endsWith(it.getKey()))
                    .sorted(Comparator.comparingInt(o -> -o.getKey().length()))
                    .map(it -> it.getValue().stream().findFirst())
                    .filter(Optional::isPresent)
                    .map(Optional::get).findFirst();
            if (firstOre.isPresent()) {
                return firstOre.get();
            }

            Optional<ResourceLocation> firstStone = ExcavatedVariants.getMappingsCache().stoneMappings.entrySet().stream()
                    .filter(it -> fullId.startsWith(it.getKey()))
                    .sorted(Comparator.comparingInt(o -> -o.getKey().length()))
                    .map(Map.Entry::getValue).findFirst();
            if (firstStone.isPresent()) {
                return firstStone.get();
            }
        }
        return null;
    }
}
