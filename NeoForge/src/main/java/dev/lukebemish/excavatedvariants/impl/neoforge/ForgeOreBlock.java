/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jspecify.annotations.NonNull;

public class ForgeOreBlock extends ModifiedOreBlock implements IForgeBlock {
    public ForgeOreBlock(ExcavatedVariants.VariantFuture future) {
        super(future);
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
