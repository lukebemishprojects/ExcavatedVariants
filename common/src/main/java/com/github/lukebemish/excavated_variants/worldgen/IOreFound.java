package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;

import java.util.List;

public interface IOreFound {
    Pair<BaseOre, List<BaseStone>> excavated_variants$get_pair();
    void excavated_variants$set_pair(Pair<BaseOre, List<BaseStone>> p);

    BaseStone excavated_variants$get_stone();

    void excavated_variants$set_stone(BaseStone stone);
}
