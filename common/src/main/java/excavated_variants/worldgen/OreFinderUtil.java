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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreFinderUtil {
    private static Map<Block, Pair<BaseOre,List<BaseStone>>> lookupMap;

    @Nullable
    public static Pair<BaseOre, List<BaseStone>> getBaseOre(BlockState state) {
        ExcavatedVariants.setupMap();
        if (!ExcavatedVariants.isMapSetupCorrectly()) {
            return null;
        }
        if (lookupMap == null) {
            lookupMap = new HashMap<>();
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
                for (Block block : pair.first().pairedBlocks) {
                    lookupMap.put(block, pair);
                }
            }
        }
        Block testing = state.getBlock();
        if (lookupMap.containsKey(testing)) {
            return lookupMap.get(testing);
        }
        return null;
    }
}
