package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.excavated_variants.mixin.IBiomeGenerationSettingsMixin;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeInjector {
    private static int addingOrdinal = 0;

    public static void addFeatures(Iterable<Biome> biomes) {
        ExcavatedVariants.LOGGER.info("Feature injection started...");
        long start = System.currentTimeMillis();

        for (Biome biome : biomes) {
            addSingleFeature(biome, Holder.direct(ExcavatedVariants.ORE_REPLACER_PLACED));
        }
        long timeTook = System.currentTimeMillis() - start;
        ExcavatedVariants.LOGGER.info("Feature injection took {} ms to complete.", timeTook);
    }

    public static void addSingleFeature(Biome biome, Holder<PlacedFeature> supplier) {
        List<HolderSet<PlacedFeature>> biomeFeatures = biome.getGenerationSettings().features();
        ArrayList<ArrayList<Holder<PlacedFeature>>> featureList = biomeFeatures.stream().map(Lists::newArrayList).collect(Collectors.toCollection(ArrayList::new));
        if (addingOrdinal == 0) {
            addingOrdinal = featureList.size();
        }
        while (featureList.size() <= addingOrdinal) {
            featureList.add(Lists.newArrayList());
        }
        featureList.get(addingOrdinal).add(supplier);
        List<HolderSet<PlacedFeature>> outList = featureList.stream().map((x) -> (HolderSet<PlacedFeature>)HolderSet.direct(x)).toList();
        ((IBiomeGenerationSettingsMixin) biome.getGenerationSettings()).setFeatures(outList);
    }
}
