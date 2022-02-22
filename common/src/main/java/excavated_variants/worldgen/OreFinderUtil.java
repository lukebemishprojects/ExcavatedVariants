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
    private static Map<ResourceLocation, Pair<BaseOre,List<BaseStone>>> lookupMap;

    public static void reset() {
        lookupMap = null;
    }

    @Nullable
    public static Pair<BaseOre, List<BaseStone>> getBaseOre(BlockState state) {
        if (!ExcavatedVariants.setupMap()) {
            return null;
        }
        if (lookupMap == null) {
            lookupMap = new HashMap<>();
            for (Pair<BaseOre, List<BaseStone>> pair : ExcavatedVariants.oreStoneList) {
                ArrayList<Block> pairedBlocks = new ArrayList<>();
                for (ResourceLocation rl : pair.first().rl_block_id) {
                    Block block = RegistryUtil.getBlockById(rl);
                    if (block != null) {
                        pairedBlocks.add(block);
                    }
                }
                for (Block block : pairedBlocks) {
                    lookupMap.put(RegistryUtil.getRlByBlock(block), pair);
                }
            }
        }
        ResourceLocation testing = RegistryUtil.getRlByBlock(state.getBlock());
        if (lookupMap.containsKey(testing)) {
            return lookupMap.get(testing);
        }
        return null;
    }
}
