package io.github.lukebemish.excavated_variants.fabric;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.IMainPlatformTarget;
import io.github.lukebemish.excavated_variants.ModifiedOreBlock;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.worldgen.OreReplacer;
import com.google.auto.service.AutoService;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.function.Supplier;

@AutoService(IMainPlatformTarget.class)
public class MainPlatformTargetImpl implements IMainPlatformTarget {
    public static final Feature<NoneFeatureConfiguration> ORE_REPLACER = new OreReplacer();
    static {}
    public static final ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED = new ConfiguredFeature<>(ORE_REPLACER, FeatureConfiguration.NONE);
    public static final PlacedFeature ORE_REPLACER_PLACED = new PlacedFeature(Holder.direct(ORE_REPLACER_CONFIGURED), List.of());
    public void registerFeatures() {
        Registry.register(Registry.FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_CONFIGURED);
        Registry.register(BuiltinRegistries.PLACED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_PLACED);
    }
    public ModifiedOreBlock makeDefaultOreBlock(String id, BaseOre ore, BaseStone stone) {
        return new ModifiedOreBlock(ore, stone);
    }

    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier) {
        RecipeSerializer<T> out = Registry.register(Registry.RECIPE_SERIALIZER,new ResourceLocation(ExcavatedVariants.MOD_ID,name),supplier.get());
        return ()->out;
    }
}
