package dev.lukebemish.excavatedvariants.worldgen;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;

import java.util.HashSet;

public interface IOreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$getPair();

    void excavated_variants$setPair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$getStone();

    void excavated_variants$setStone(BaseStone stone);
}
