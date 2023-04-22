package dev.lukebemish.excavatedvariants.mixin;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.worldgen.OreFound;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;

@Mixin(Block.class)
public class BlockMixin implements OreFound {
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
