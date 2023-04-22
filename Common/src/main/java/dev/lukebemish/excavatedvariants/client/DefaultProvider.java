package dev.lukebemish.excavatedvariants.client;

import java.util.List;
import java.util.function.BiConsumer;

import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.ResourceProvider;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProviderMap;

import net.minecraft.resources.ResourceLocation;

@ExcavatedVariantsListener
public class DefaultProvider implements ResourceProvider {
    @Override
    public void provideOreTextures(List<ResourceLocation> ores, BiConsumer<ResourceLocation, List<TexFaceProviderMap>> textureProducerConsumer) {
        /*
         * - Reading in the blockstates of every ore and stone involved
         * - From each blockstate, getting a list of models involved - this will be a *single list*, not a key-value
         *   map, as we only care about the "primary" model and its variants (and we assume that each orientation uses
         *   the same "primary" model and variants).
         * - Now we have a list of models per blockstate. For each model:
         *    - extract the textures used in the model
         */
        for (ResourceLocation ore : ores) {
            List<ResourceLocation> blockStates = getBlockStates(ore);
            if (blockStates == null || blockStates.isEmpty()) {
                ExcavatedVariants.LOGGER.warn("Could not find blockstates for ore " + ore);
                continue;
            }
        }
    }

    @Override
    public void provideStoneTextures(List<ResourceLocation> stones, BiConsumer<ResourceLocation, List<ModelData>> textureConsumer) {

    }

    private List<ResourceLocation> getBlockStates(ResourceLocation block) {
        return null;
    }

    @Override
    public int priority() {
        return ExcavatedVariants.DEFAULT_COMPAT_PRIORITY;
    }
}
