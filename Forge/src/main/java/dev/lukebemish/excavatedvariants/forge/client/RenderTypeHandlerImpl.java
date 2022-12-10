package dev.lukebemish.excavatedvariants.forge.client;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.client.IRenderTypeHandler;
import net.minecraft.world.level.block.Block;

@AutoService(IRenderTypeHandler.class)
public class RenderTypeHandlerImpl implements IRenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
    }
}
