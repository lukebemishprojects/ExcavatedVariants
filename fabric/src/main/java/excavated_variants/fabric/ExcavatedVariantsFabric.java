package excavated_variants.fabric;

import excavated_variants.ExcavatedVariants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ExcavatedVariantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExcavatedVariants.init();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            ResourceKey<PlacedFeature> confKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer"));
            // A bit hacky, but will hopefully put it after existing stuff (like Unearthed's generation)
            BiomeModifications.create(confKey.location()).add(ModificationPhase.POST_PROCESSING, (x)->true, context -> {
                context.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, confKey);
            });;
        }
    }
}
