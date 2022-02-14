package excavated_variants.fabric;

import excavated_variants.ExcavatedVariants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;

public class ExcavatedVariantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExcavatedVariants.init();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            BiomeModifications.addFeature((x)->true, GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY,new ResourceLocation(ExcavatedVariants.MOD_ID,"ore_replacer")));
        }
    }
}
