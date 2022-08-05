package io.github.lukebemish.excavated_variants.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.Properties.class)
public interface IBlockPropertiesMixin {

    @Accessor
    boolean getIsRandomlyTicking();

    @Accessor
    void setIsRandomlyTicking(boolean isRandomlyTicking);

    @Accessor
    void setDynamicShape(boolean dynamicShape);

    @Accessor
    void setHasCollision(boolean hasCollision);

    @Accessor
    ToIntFunction<BlockState> getLightEmission();

    @Accessor
    void setLightEmission(ToIntFunction<BlockState> lightEmission);
}
