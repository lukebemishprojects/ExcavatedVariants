/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;

@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "2.3.0")
public interface IOreListModifier extends ICompatPlugin {
    List<Pair<BaseOre, HashSet<BaseStone>>> modify(List<Pair<BaseOre, HashSet<BaseStone>>> oreStoneList, Collection<BaseStone> stones);
}
