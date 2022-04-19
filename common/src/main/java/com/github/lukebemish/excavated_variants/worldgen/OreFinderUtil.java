package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.util.Pair;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
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
                ArrayList<ResourceLocation> pairedBlocks = new ArrayList<>();
                for (ResourceLocation rl : pair.first().block_id) {
                    Block block = RegistryUtil.getBlockById(rl);
                    if (block != null) {
                        pairedBlocks.add(rl);
                    }
                }
                for (BaseStone stone : pair.last()) {
                    ResourceLocation rl = new ResourceLocation(ExcavatedVariants.MOD_ID,stone.id+"_"+pair.first().id);
                    Block block = RegistryUtil.getBlockById(rl);
                    if (block != null) {
                        pairedBlocks.add(rl);
                    }
                }
                for (ResourceLocation rl : pairedBlocks) {
                    lookupMap.put(rl, pair);
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
