package io.github.lukebemish.excavated_variants.worldgen;

import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.util.Pair;

import java.util.HashSet;

public interface IOreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$get_pair();
    void excavated_variants$set_pair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$get_stone();

    void excavated_variants$set_stone(BaseStone stone);
}
