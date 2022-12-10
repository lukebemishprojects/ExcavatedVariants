package dev.lukebemish.excavatedvariants.forge.compat;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.platform.Services;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
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
                for (ResourceLocation block_id : p.getFirst().block_id) {
                    Block baseOreBlock = Services.REGISTRY_UTIL.getBlockById(block_id);
                    excavatedVariantsOres.put(baseOreBlock, p);
                }
            }
        }

        IStoneType stoneType = event.getStoneType();
        Block baseBlock = stoneType.getBaseBlock().getBlock();
        excavatedVariantsOres.forEach((baseOreBlock, pair) -> {
            HashSet<BaseStone> stones = pair.getSecond();
            stones.stream().filter(stone -> Services.REGISTRY_UTIL.getBlockById(stone.block_id) == baseBlock).findAny().ifPresent(stone -> {
                Block oreBlock = Services.REGISTRY_UTIL.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.getFirst().id));
                if (oreBlock != null) {
                    stoneType.getOreMap().put(baseOreBlock, oreBlock.defaultBlockState());
                }
            });
        });
    }
}
