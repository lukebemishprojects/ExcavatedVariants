package io.github.lukebemish.excavated_variants.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.worldgen.IOreFound;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;

@Mixin(Block.class)
public class BlockMixin implements IOreFound {
    @Unique
    private Pair<BaseOre, HashSet<BaseStone>> excavated_variants$ore_pair;
    @Unique
    private BaseStone excavated_variants$stone;

    @Override
    public Pair<BaseOre, HashSet<BaseStone>> excavated_variants$getPair() {
        return excavated_variants$ore_pair;
    }

    @Override
    public void excavated_variants$setPair(Pair<BaseOre, HashSet<BaseStone>> p) {
        this.excavated_variants$ore_pair = p;
    }

    @Override
    public BaseStone excavated_variants$getStone() {
        return excavated_variants$stone;
    }

    @Override
    public void excavated_variants$setStone(BaseStone stone) {
        this.excavated_variants$stone = stone;
    }
}
