package com.github.lukebemish.excavated_variants.forge;

import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;

public class ExcavatedVariantsImpl {
    public static void registerFeatures() {
    }
    public static ModifiedOreBlock makeDefaultOreBlock(String id, BaseOre ore, BaseStone stone) {
        return new ForgeOreBlock(ore, stone);
    }
}
