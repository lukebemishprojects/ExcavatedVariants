/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.worldgen;

import java.util.HashSet;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;

public interface OreFound {
    Pair<BaseOre, HashSet<BaseStone>> excavated_variants$getPair();

    void excavated_variants$setPair(Pair<BaseOre, HashSet<BaseStone>> p);

    BaseStone excavated_variants$getStone();

    void excavated_variants$setStone(BaseStone stone);
}
