/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge.compat;

public class HyleCompat {
    /*private Map<Block, Pair<BaseOre, HashSet<BaseStone>>> excavatedVariantsOres;

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
    }*/
}
