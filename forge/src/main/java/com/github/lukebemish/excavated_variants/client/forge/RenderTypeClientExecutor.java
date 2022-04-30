package com.github.lukebemish.excavated_variants.client.forge;

import com.github.lukebemish.excavated_variants.forge.ExcavatedVariantsForgeClient;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class RenderTypeClientExecutor {
    public static void setMipped(Block block) {
        Runnable exec = () -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped());
        ExcavatedVariantsForgeClient.queue.add(exec);
    }
}
