/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt.client;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.client.RenderTypeHandler;
import net.fabricmc.api.EnvType;
import net.minecraft.world.level.block.Block;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

@AutoService(RenderTypeHandler.class)
public class RenderTypeHandlerImpl implements RenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
        if (MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.CLIENT)) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}