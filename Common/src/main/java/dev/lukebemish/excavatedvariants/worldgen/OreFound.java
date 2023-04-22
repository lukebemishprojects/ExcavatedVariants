package dev.lukebemish.excavatedvariants.worldgen;

import java.util.HashSet;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;

public interface OreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$getPair();

    void excavated_variants$setPair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$getStone();

    void excavated_variants$setStone(BaseStone stone);
}
