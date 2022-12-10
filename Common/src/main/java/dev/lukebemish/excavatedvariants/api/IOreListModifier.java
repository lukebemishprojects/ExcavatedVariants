package dev.lukebemish.excavatedvariants.api;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.data.BaseOre;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public interface IOreListModifier extends ICompatPlugin {
    List<Pair<BaseOre, HashSet<BaseStone>>> modify(List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList, Collection<BaseStone> stones);
}
