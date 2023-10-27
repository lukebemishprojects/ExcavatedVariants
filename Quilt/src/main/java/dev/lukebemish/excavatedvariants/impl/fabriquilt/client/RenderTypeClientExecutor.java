/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class RenderTypeClientExecutor {
    @SuppressWarnings("deprecation")
    public static void setMipped(Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutoutMipped());
    }
}
