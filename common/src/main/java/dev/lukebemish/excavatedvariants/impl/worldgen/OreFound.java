/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.worldgen;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;

public interface OreFound {
    Ore excavated_variants$getOre();

    void excavated_variants$setOre(Ore o);

    Stone excavated_variants$getOreStone();

    void excavated_variants$setOreStone(Stone o);

    Stone excavated_variants$getStone();

    void excavated_variants$setStone(Stone stone);
}
