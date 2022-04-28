package com.github.lukebemish.excavated_variants.worldgen;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.util.Pair;

import java.util.List;

public interface IOreFound {
    Pair<BaseOre, List<BaseStone>>  excavated_variants$get();
    void excavated_variants$set(Pair<BaseOre, List<BaseStone>> p);
}
