package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;

import java.util.HashSet;

public interface IOreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$get_pair();
    void excavated_variants$set_pair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$get_stone();

    void excavated_variants$set_stone(BaseStone stone);
}
