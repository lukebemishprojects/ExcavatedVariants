package excavated_variants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import excavated_variants.mixin.BiomeGenerationSettingsMixin;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BiomeInjector {
    private static int addingOrdinal = 0;

    public static void addFeatures(Iterable<Biome> biomes, RegistryAccess registryAccess) {
        ExcavatedVariants.LOGGER.info("Feature injection started...");
        long start = System.currentTimeMillis();

        for (Biome biome : biomes) {
            addSingleFeature(biome, () -> BuiltinRegistries.PLACED_FEATURE.get(new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer")));
        }
        long timeTook = System.currentTimeMillis() - start;
        ExcavatedVariants.LOGGER.info("Feature injection took {} ms to complete.", timeTook);
    }

    public static void addSingleFeature(Biome biome, Supplier<PlacedFeature> supplier) {
        List<List<Supplier<PlacedFeature>>> biomeFeatures = biome.getGenerationSettings().features();
        biomeFeatures = biomeFeatures instanceof ImmutableList ? biomeFeatures.stream().map(Lists::newArrayList).collect(Collectors.toList()) : biomeFeatures;
        if (addingOrdinal == 0) {
            addingOrdinal = biomeFeatures.size();
        }
        while (biomeFeatures.size() <= addingOrdinal) {
            biomeFeatures.add(Lists.newArrayList());
        }
        biomeFeatures.get(addingOrdinal).add(supplier);
        ((BiomeGenerationSettingsMixin) biome.getGenerationSettings()).setFeatures(biomeFeatures.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()));
    }
}
