package dev.lukebemish.excavatedvariants.impl.quilt.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class RenderTypeClientExecutor {
    public static void setMipped(Block block) {
        BlockRenderLayerMap.put(RenderType.cutoutMipped(), block);
    }
}
