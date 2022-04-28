package com.github.lukebemish.excavated_variants.mixin;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;
import com.github.lukebemish.excavated_variants.worldgen.IOreFound;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin implements IOreFound {
    @Unique
    private Pair<BaseOre, List<BaseStone>> excavated_variants$ore_pair;

    @Override
    public Pair<BaseOre, List<BaseStone>> excavated_variants$get() {
        return excavated_variants$ore_pair;
    }

    @Override
    public void excavated_variants$set(Pair<BaseOre, List<BaseStone>> p) {
        excavated_variants$ore_pair = p;
    }
}
