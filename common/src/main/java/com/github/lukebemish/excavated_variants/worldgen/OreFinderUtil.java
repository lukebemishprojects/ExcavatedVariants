package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;

public class OreFinderUtil {

    public static void setupBlocks() {
        for (Block block : RegistryUtil.getAllBlocks()) {
            ((IOreFound)block).excavated_variants$set_pair(null);
            ((IOreFound) block).excavated_variants$set_stone(null);
            if (ExcavatedVariants.setupMap()) {
                for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                    for (ResourceLocation rl : p.first().block_id) {
                        Block bl2 = RegistryUtil.getBlockById(rl);
                        if (bl2==block) {
                            ((IOreFound)block).excavated_variants$set_pair(p);
                        }
                    }
                    for (BaseStone stone : p.last()) {
                        if (stone.block_id.equals(RegistryUtil.getRlByBlock(block))) {
                            ((IOreFound) block).excavated_variants$set_stone(stone);
                        }
                    }
                }
            }
        }
    }
}
