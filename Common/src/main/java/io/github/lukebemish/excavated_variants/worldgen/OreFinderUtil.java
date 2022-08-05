package io.github.lukebemish.excavated_variants.worldgen;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;

public class OreFinderUtil {

    public static void setupBlocks() {
        for (Block block : Services.REGISTRY_UTIL.getAllBlocks()) {
            ((IOreFound)block).excavated_variants$set_pair(null);
            ((IOreFound)block).excavated_variants$set_stone(null);
            if (ExcavatedVariants.setupMap()) {
                for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                    for (ResourceLocation rl : p.getFirst().block_id) {
                        Block bl2 = Services.REGISTRY_UTIL.getBlockById(rl);
                        if (bl2==block) {
                            ((IOreFound)block).excavated_variants$set_pair(p);
                        }
                    }
                    for (BaseStone stone : p.getSecond()) {
                        if (stone.block_id.equals(Services.REGISTRY_UTIL.getRlByBlock(block))) {
                            ((IOreFound) block).excavated_variants$set_stone(stone);
                        }
                    }
                }
            }
        }
    }
}
