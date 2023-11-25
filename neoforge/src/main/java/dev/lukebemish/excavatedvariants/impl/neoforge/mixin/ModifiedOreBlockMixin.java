/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge.mixin;

import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModifiedOreBlock.class, remap = false)
public abstract class ModifiedOreBlockMixin extends DropExperienceBlock implements IBlockExtension {
    @Shadow
    @Final
    protected Block target;

    @Shadow
    @Final
    protected boolean delegateSpecialDrops;

    public ModifiedOreBlockMixin(Properties pProperties) {
        super(pProperties);
        throw new IllegalStateException();
    }

    @Override
    public int getExpDrop(@NonNull BlockState state, @NonNull LevelReader level, @NonNull RandomSource randomSource, @NonNull BlockPos pos, int fortune, int silktouch) {
        if (target != null && this.delegateSpecialDrops) {
            return target.getExpDrop(target.defaultBlockState(), level, randomSource, pos, fortune, silktouch);
        } else {
            return super.getExpDrop(state, level, randomSource, pos, fortune, silktouch);
        }
    }
}
