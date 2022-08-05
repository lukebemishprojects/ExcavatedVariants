package io.github.lukebemish.excavated_variants.worldgen;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

import java.util.HashSet;

public interface IOreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$getPair();

    void excavated_variants$setPair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$getStone();

    void excavated_variants$setStone(BaseStone stone);
}
