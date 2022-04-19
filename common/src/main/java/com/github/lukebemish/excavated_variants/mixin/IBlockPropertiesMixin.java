package com.github.lukebemish.excavated_variants.mixin;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.Properties.class)
public interface IBlockPropertiesMixin {
    @Accessor
    Material getMaterial();
    @Accessor
    SoundType getSoundType();
    @Accessor
    float getExplosionResistance();
    @Accessor
    boolean getIsRandomlyTicking();
    @Accessor
    float getFriction();
    @Accessor
    float getSpeedFactor();
    @Accessor
    float getJumpFactor();
    @Accessor
    float getDestroyTime();
    @Accessor
    void setDynamicShape(boolean dynamicShape);
    @Accessor
    void setHasCollision(boolean hasCollision);
    @Accessor
    ToIntFunction<BlockState> getLightEmission();
    @Accessor
    void setIsRandomlyTicking(boolean isRandomlyTicking);
    @Accessor
    void setLightEmission(ToIntFunction<BlockState> lightEmission);
    @Accessor
    BlockBehaviour.StatePredicate getIsRedstoneConductor();
    @Accessor
    BlockBehaviour.StatePredicate getIsSuffocating();
    @Accessor
    BlockBehaviour.StatePredicate getIsViewBlocking();
    @Accessor
    BlockBehaviour.StatePredicate getHasPostProcess();
    @Accessor
    BlockBehaviour.StatePredicate getEmissiveRendering();
}
