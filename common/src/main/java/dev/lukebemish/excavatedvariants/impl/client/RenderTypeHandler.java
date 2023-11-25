/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import net.minecraft.world.level.block.Block;

public interface RenderTypeHandler {
    void setRenderTypeMipped(Block block);
}