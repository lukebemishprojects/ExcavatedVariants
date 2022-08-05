package io.github.lukebemish.excavated_variants.quilt.compat;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import lilypuree.hyle.compat.StoneTypeCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

public class HyleCompat {
    private static Map<Block, Pair<BaseOre, HashSet<BaseStone>>> excavatedVariantsOres;

    public static void init() {
        StoneTypeCallback.EVENT.register(stoneType -> {
            if (excavatedVariantsOres == null) {
                excavatedVariantsOres = new IdentityHashMap<>();
                for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                    for (ResourceLocation block_id : p.getFirst().block_id) {
                        Block baseOreBlock = Services.REGISTRY_UTIL.getBlockById(block_id);
                        excavatedVariantsOres.put(baseOreBlock, p);
                    }
                }
            }

            Block baseBlock = stoneType.getBaseBlock().getBlock();
            excavatedVariantsOres.forEach((baseOreBlock, pair) -> {
                HashSet<BaseStone> stones = pair.getSecond();
                stones.stream().filter(stone -> Services.REGISTRY_UTIL.getBlockById(stone.block_id) == baseBlock).findAny().ifPresent(stone -> {
                    Block oreBlock = Services.REGISTRY_UTIL.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.getFirst().id));
                    if (!stoneType.getOreMap().containsKey(baseOreBlock) && oreBlock != null)
                        stoneType.getOreMap().put(baseOreBlock, oreBlock.defaultBlockState());
                });
            });
            return true;
        });
    }
}
