package io.github.lukebemish.excavated_variants.mixin;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface IBlockBehaviorMixin {
    @Mutable
    @Final
    @Accessor
    void setMaterial(Material material);
    @Mutable
    @Final
    @Accessor
    void setExplosionResistance(float explosionResistance);
    @Mutable
    @Final
    @Accessor
    void setIsRandomlyTicking(boolean isRandomlyTicking);
    @Mutable
    @Final
    @Accessor
    void setSoundType(SoundType soundType);
    @Mutable
    @Final
    @Accessor
    void setFriction(float friction);
    @Mutable
    @Final
    @Accessor
    void setSpeedFactor(float speedFactor);
    @Mutable
    @Final
    @Accessor
    void setJumpFactor(float jumpFactor);
    @Mutable
    @Final
    @Accessor
    void setProperties(BlockBehaviour.Properties properties);
}
