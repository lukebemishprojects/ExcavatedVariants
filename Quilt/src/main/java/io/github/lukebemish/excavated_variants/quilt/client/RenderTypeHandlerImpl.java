package io.github.lukebemish.excavated_variants.quilt.client;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.client.IRenderTypeHandler;
import net.fabricmc.api.EnvType;
import net.minecraft.world.level.block.Block;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

@AutoService(IRenderTypeHandler.class)
public class RenderTypeHandlerImpl implements IRenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
        if (MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.CLIENT)) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}