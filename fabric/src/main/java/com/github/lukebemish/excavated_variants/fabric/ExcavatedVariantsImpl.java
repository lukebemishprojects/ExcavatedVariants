package com.github.lukebemish.excavated_variants.fabric;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.worldgen.OreReplacer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Material;

import java.util.List;

public class ExcavatedVariantsImpl {
    public static final Feature<NoneFeatureConfiguration> ORE_REPLACER = new OreReplacer();
    static {}
    public static final ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED = new ConfiguredFeature<>(ORE_REPLACER, FeatureConfiguration.NONE);
    public static final PlacedFeature ORE_REPLACER_PLACED = new PlacedFeature(Holder.direct(ORE_REPLACER_CONFIGURED), List.of());
    public static void registerFeatures() {
        Registry.register(Registry.FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_CONFIGURED);
        Registry.register(BuiltinRegistries.PLACED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_PLACED);
    }
    public static ModifiedOreBlock makeDefaultOreBlock(String id, BaseOre ore, BaseStone stone) {
        return new ModifiedOreBlock(ore, stone);
    }
}
