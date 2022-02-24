package excavated_variants.forge;

import excavated_variants.ModifiedOreBlock;
import excavated_variants.RegistryUtil;
import excavated_variants.data.BaseOre;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.NotNull;

public class ForgeOreBlock extends ModifiedOreBlock implements IForgeBlock {
    private final BaseOre ore;

    public ForgeOreBlock(Properties properties, BaseOre ore) {
        super(properties, ore);
        this.ore = ore;
    }

    @Override
    public int getExpDrop(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, int fortune, int silktouch) {
        Block target = RegistryUtil.getBlockById(ore.rl_block_id.get(0));
        if (target != null) {
            return target.getExpDrop(target.defaultBlockState(), level, pos, fortune, silktouch);
        } else {
            return super.getExpDrop(state,level,pos,fortune,silktouch);
        }
    }
}
