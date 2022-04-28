package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreFinderUtil {
    private static Map<ResourceLocation, Pair<BaseOre,List<BaseStone>>> lookupMap;

    private static void reset() {
        lookupMap = null;
    }

    public static void setupBlocks() {
        reset();
        for (Block block : Registry.BLOCK) {
            ((IOreFound)block).excavated_variants$set_pair(getBaseOre(block));
            ((IOreFound) block).excavated_variants$set_stone(null);
            if (ExcavatedVariants.setupMap()) {
                for (Pair<BaseOre, List<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                    for (BaseStone stone : p.last()) {
                        if (stone.block_id.equals(RegistryUtil.getRlByBlock(block))) {
                            ((IOreFound) block).excavated_variants$set_stone(stone);
                        }
                    }
                }
            }
        }
    }

    private static Pair<BaseOre, List<BaseStone>> getBaseOre(Block block2) {
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
        ResourceLocation testing = RegistryUtil.getRlByBlock(block2);
        if (lookupMap.containsKey(testing)) {
            return lookupMap.get(testing);
        }
        return null;
    }
}
