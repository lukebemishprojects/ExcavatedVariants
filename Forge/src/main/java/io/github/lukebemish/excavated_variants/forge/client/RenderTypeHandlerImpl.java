package io.github.lukebemish.excavated_variants.forge.client;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.client.IRenderTypeHandler;
import net.minecraft.world.level.block.Block;

@AutoService(IRenderTypeHandler.class)
public class RenderTypeHandlerImpl implements IRenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
    }
}
