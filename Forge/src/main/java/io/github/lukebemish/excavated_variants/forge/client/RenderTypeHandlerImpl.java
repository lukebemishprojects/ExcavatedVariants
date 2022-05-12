package io.github.lukebemish.excavated_variants.forge.client;

import io.github.lukebemish.excavated_variants.client.IRenderTypeHandler;
import com.google.auto.service.AutoService;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

@AutoService(IRenderTypeHandler.class)
public class RenderTypeHandlerImpl implements IRenderTypeHandler {
    public void setRenderTypeMipped(Block block) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderTypeClientExecutor.setMipped(block);
        }
    }
}
