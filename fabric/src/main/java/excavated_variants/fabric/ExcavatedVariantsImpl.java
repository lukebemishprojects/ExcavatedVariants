package excavated_variants.fabric;

import excavated_variants.ExcavatedVariants;
import excavated_variants.worldgen.OreReplacer;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ExcavatedVariantsImpl {
    public static final Feature<NoneFeatureConfiguration> ORE_REPLACER = new OreReplacer();
    public static final ConfiguredFeature<NoneFeatureConfiguration,?> ORE_REPLACER_CONFIGURED = ORE_REPLACER.configured(NoneFeatureConfiguration.NONE);
    public static final PlacedFeature ORE_REPLACER_PLACED = ORE_REPLACER_CONFIGURED.placed();
    public static void registerFeatures() {
        Registry.register(Registry.FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_CONFIGURED);
        Registry.register(BuiltinRegistries.PLACED_FEATURE,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"),ORE_REPLACER_PLACED);
    }
}
