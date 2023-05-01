/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.platform.services;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface RegistryUtil {
    void reset();

    @Nullable
    Block getBlockById(ResourceLocation rl);

    @Nullable
    Item getItemById(ResourceLocation rl);

    @Nullable
    ResourceLocation getRlByBlock(Block block);

    Iterable<Block> getAllBlocks();
}
