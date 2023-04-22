package dev.lukebemish.excavatedvariants.forge.client;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.client.RenderTypeHandler;
import net.minecraft.world.level.block.Block;

@AutoService(RenderTypeHandler.class)
public class RenderTypeHandlerImpl implements RenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
    }
}
