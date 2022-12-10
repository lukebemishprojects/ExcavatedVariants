package dev.lukebemish.excavatedvariants.forge;

import dev.lukebemish.excavatedvariants.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.NotNull;

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
