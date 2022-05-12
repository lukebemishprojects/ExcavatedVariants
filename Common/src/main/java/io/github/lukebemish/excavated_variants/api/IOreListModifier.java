package io.github.lukebemish.excavated_variants.api;

import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public interface IOreListModifier extends ICompatPlugin {
    List<Pair<BaseOre, HashSet<BaseStone>>> modify(List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList, Collection<BaseStone> stones);
}
