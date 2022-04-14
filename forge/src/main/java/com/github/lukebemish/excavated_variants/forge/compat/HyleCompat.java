package com.github.lukebemish.excavated_variants.forge.compat;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.Pair;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import lilypuree.hyle.compat.IStoneType;
import lilypuree.hyle.compat.StoneTypeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class HyleCompat {
    private Map<Block, Pair<BaseOre, List<BaseStone>>> excavatedVariantsOres;

    @SubscribeEvent
    public void onStoneTypeEvent(StoneTypeEvent event) {
        if (excavatedVariantsOres == null) {
            excavatedVariantsOres = new IdentityHashMap<>();
            for (Pair<BaseOre, List<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                for (ResourceLocation block_id : p.first().rl_block_id) {
                    Block baseOreBlock = RegistryUtil.getBlockById(block_id);
                    excavatedVariantsOres.put(baseOreBlock, p);
                }
            }
        }

        IStoneType stoneType = event.getStoneType();
        Block baseBlock = stoneType.getBaseBlock().getBlock();
        excavatedVariantsOres.forEach((baseOreBlock, pair) -> {
            List<BaseStone> Stones = pair.last();
            Stones.stream().filter(stone -> RegistryUtil.getBlockById(stone.rl_block_id) == baseBlock).findAny().ifPresent(stone -> {
                Block oreBlock = RegistryUtil.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.first().id));
                stoneType.getOreMap().put(baseOreBlock, oreBlock.defaultBlockState());
            });
        });
    }
}
