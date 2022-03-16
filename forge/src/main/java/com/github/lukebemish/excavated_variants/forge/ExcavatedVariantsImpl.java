package com.github.lukebemish.excavated_variants.forge;

import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class ExcavatedVariantsImpl {
    public static void registerFeatures() {
    }
    public static ModifiedOreBlock makeDefaultOreBlock(String id, BaseOre ore) {
        return new ForgeOreBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f), ore);
    }
}
