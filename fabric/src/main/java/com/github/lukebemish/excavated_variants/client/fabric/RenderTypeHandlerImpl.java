package com.github.lukebemish.excavated_variants.client.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;

public class RenderTypeHandlerImpl {
    public static void setRenderTypeMipped(Block block) {
        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}