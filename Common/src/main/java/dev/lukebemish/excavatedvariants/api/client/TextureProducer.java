package dev.lukebemish.excavatedvariants.api.client;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface TextureProducer {
    /**
     * Produces a new ore texture source from the given texture sources.
     * @param newStoneSource the texture source of the stone to be transferred to
     * @param oldStoneSource the texture source of the stone to be transferred from
     * @return a pair of the new texture source and a list of the resource locations of textures used in generating the
     * new texture source
     */
    Pair<ITexSource, List<ResourceLocation>> produce(Function<SourceWrapper, ITexSource> newStoneSource, Function<SourceWrapper, ITexSource> oldStoneSource);

    interface SourceWrapper {
        ITexSource wrap(ITexSource source);
    }
}
