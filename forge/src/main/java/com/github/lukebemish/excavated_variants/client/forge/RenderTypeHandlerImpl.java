package com.github.lukebemish.excavated_variants.client.forge;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class RenderTypeHandlerImpl {
    public static void setRenderTypeMipped(Block block) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}
