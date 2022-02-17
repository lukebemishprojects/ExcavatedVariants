package excavated_variants.worldgen;

import excavated_variants.ExcavatedVariants;
import excavated_variants.Pair;
import excavated_variants.RegistryUtil;
import excavated_variants.data.BaseOre;
import excavated_variants.data.BaseStone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OreFinderUtil {
    private static final List<Pair<Block, Pair<BaseOre, List<BaseStone>>>> lookup = new CopyOnWriteArrayList<>();
    private static  final List<Block> nullList = new CopyOnWriteArrayList<>();

    @Nullable
    public static Pair<BaseOre, List<BaseStone>> getBaseOre(BlockState state) {
        ExcavatedVariants.setupMap();
        if (!ExcavatedVariants.isMapSetupCorrectly()) {
            return null;
        }
        Block testing = state.getBlock();
        if (nullList.stream().anyMatch(state::is)) {
            return null;
        }
        for (Pair<Block, Pair<BaseOre, List<BaseStone>>> p : lookup) {
            if (state.is(p.first())) {
                return p.last();
            }
        }
        for (Pair<BaseOre, List<BaseStone>> pair : ExcavatedVariants.oreStoneList) {
            if (pair.first().pairedBlocks == null || pair.first().pairedBlocks.size() < 1) {
                pair.first().pairedBlocks = new ArrayList<>();
                for (ResourceLocation rl : pair.first().rl_block_id) {
                    Block block = RegistryUtil.getBlockById(rl);
                    if (block != null) {
                        pair.first().pairedBlocks.add(block);
                    }
                }
            }
            if (pair.first().pairedBlocks.size() >= 1 && pair.first().pairedBlocks.stream().anyMatch(state::is)) {
                lookup.add(new Pair<>(testing,pair));
                return pair;
            }
        }
        nullList.add(testing);
        return null;
    }
}
