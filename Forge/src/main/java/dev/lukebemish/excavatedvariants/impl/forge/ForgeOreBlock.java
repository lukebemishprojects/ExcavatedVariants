/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeOreBlock extends ModifiedOreBlock implements IForgeBlock {
    public ForgeOreBlock(BaseOre ore, BaseStone stone) {
        super(ore, stone);
    }

    @Override
    public int getExpDrop(@NotNull BlockState state, @NotNull LevelReader level, @NotNull RandomSource randomSource, @NotNull BlockPos pos, int fortune, int silktouch) {
        if (target != null && this.delegateSpecialDrops) {
            return target.getExpDrop(target.defaultBlockState(), level, randomSource, pos, fortune, silktouch);
        } else {
            return super.getExpDrop(state, level, randomSource, pos, fortune, silktouch);
        }
    }
}
