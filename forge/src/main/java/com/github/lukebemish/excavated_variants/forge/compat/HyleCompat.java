package com.github.lukebemish.excavated_variants.forge.compat;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;
import lilypuree.hyle.compat.IStoneType;
import lilypuree.hyle.compat.StoneTypeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

public class HyleCompat {
    private Map<Block, Pair<BaseOre, HashSet<BaseStone>>> excavatedVariantsOres;

    @SubscribeEvent
    public void onStoneTypeEvent(StoneTypeEvent event) {
        if (excavatedVariantsOres == null) {
            excavatedVariantsOres = new IdentityHashMap<>();
            for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                for (ResourceLocation block_id : p.first().block_id) {
                    Block baseOreBlock = RegistryUtil.getBlockById(block_id);
                    excavatedVariantsOres.put(baseOreBlock, p);
                }
            }
        }

        IStoneType stoneType = event.getStoneType();
        Block baseBlock = stoneType.getBaseBlock().getBlock();
        excavatedVariantsOres.forEach((baseOreBlock, pair) -> {
            HashSet<BaseStone> stones = pair.last();
            stones.stream().filter(stone -> RegistryUtil.getBlockById(stone.block_id) == baseBlock).findAny().ifPresent(stone -> {
                Block oreBlock = RegistryUtil.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.first().id));
                stoneType.getOreMap().put(baseOreBlock, oreBlock.defaultBlockState());
            });
        });
    }
}
