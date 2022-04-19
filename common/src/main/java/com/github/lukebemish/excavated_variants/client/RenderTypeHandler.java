package com.github.lukebemish.excavated_variants.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;

public class RenderTypeHandler {
    @ExpectPlatform
    public static void setRenderTypeMipped(Block block) {
        throw new AssertionError();
    }
}
