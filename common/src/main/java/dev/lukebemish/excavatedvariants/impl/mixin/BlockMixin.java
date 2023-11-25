/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.mixin;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFound;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements OreFound {
    @Unique
    private Stone excavated_variants$stone = null;
    @Unique
    private Stone excavated_variants$oreStone = null;
    @Unique
    private Ore excavated_variants$ore = null;

    @Override
    public Ore excavated_variants$getOre() {
        return excavated_variants$ore;
    }

    @Override
    public void excavated_variants$setOre(Ore o) {
        excavated_variants$ore = o;
    }

    @Override
    public Stone excavated_variants$getOreStone() {
        return excavated_variants$oreStone;
    }

    @Override
    public void excavated_variants$setOreStone(Stone o) {
        excavated_variants$oreStone = o;
    }

    @Override
    public Stone excavated_variants$getStone() {
        return excavated_variants$stone;
    }

    @Override
    public void excavated_variants$setStone(Stone stone) {
        excavated_variants$stone = stone;
    }
}
