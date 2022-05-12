package io.github.lukebemish.excavated_variants.quilt.client;

import io.github.lukebemish.excavated_variants.client.IRenderTypeHandler;
import com.google.auto.service.AutoService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;

@AutoService(IRenderTypeHandler.class)
public class RenderTypeHandlerImpl implements IRenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
        if (FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}