/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.worldgen;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModLifecycle;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class OreFinderUtil {

    private static final Map<Ore, Map<Stone, Block>> ORE_STONE_MAP = new IdentityHashMap<>();

    private OreFinderUtil() {
    }

    public static @Nullable Block getBlock(Ore ore, Stone newStone) {
        if (ModLifecycle.getLifecyclePhase() == ModLifecycle.POST) {
            var map = ORE_STONE_MAP.get(ore);
            if (map != null) {
                return map.get(newStone);
            }
        }
        return null;
    }

    public static void setupBlocks() {
        if (ModLifecycle.getLifecyclePhase() != ModLifecycle.POST) {
            var e = new IllegalStateException("Something has gone badly wrong with load ordering. Please report this to Excavated Variants alongside a log!");
            ExcavatedVariants.LOGGER.error("...huh? Where are we? Expected {}, but in {}", ModLifecycle.POST.name(), ModLifecycle.getLifecyclePhase().name(), e);
            throw e;
        }

        for (Ore ore : RegistriesImpl.ORE_REGISTRY) {
            Map<Stone, Block> map = new IdentityHashMap<>();
            for (var entry : ore.getBlocks().entrySet()) {
                var stone = Objects.requireNonNull(RegistriesImpl.STONE_REGISTRY.get(entry.getValue()));
                var block = Objects.requireNonNull(BuiltInRegistries.BLOCK.get(entry.getKey()));
                ((OreFound) block).excavated_variants$setOre(ore);
                ((OreFound) block).excavated_variants$setOreStone(stone);
                map.put(stone, block);
            }
            ORE_STONE_MAP.put(ore, map);
        }
        for (Stone stone : RegistriesImpl.STONE_REGISTRY) {
            var block = BuiltInRegistries.BLOCK.get(stone.block);
            if (block == null) continue;
            ((OreFound) block).excavated_variants$setStone(stone);
        }
    }
}
