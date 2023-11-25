/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockPropertiesMixin {

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
