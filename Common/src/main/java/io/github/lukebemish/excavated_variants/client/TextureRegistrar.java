package io.github.lukebemish.excavated_variants.client;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.IInputStreamSource;
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.DynamicTextureSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TextureMetaGenerator;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.AnimationFrameCapture;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.AnimationSplittingSource;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.ForegroundTransfer;
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.TextureReader;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

public class TextureRegistrar implements Supplier<IPathAwareInputStreamSource> {

    private final Collection<Pair<BaseOre,BaseStone>> originalPairs;
    private final List<Pair<BaseOre,BaseStone>> toMake;

    public TextureRegistrar(Collection<Pair<BaseOre, BaseStone>> originalPairs, List<Pair<BaseOre,BaseStone>> toMake) {
        this.originalPairs = originalPairs;
        this.toMake = toMake;
    }

    public Pair<IInputStreamSource,IInputStreamSource> setupExtractor(ResourceLocation stoneOrig, ResourceLocation ore, ResourceLocation stoneNew, ResourceLocation out) {
        stoneOrig = new ResourceLocation(stoneOrig.getNamespace(), stoneOrig.getPath().replaceFirst("textures/","").replaceFirst("\\.png",""));
        stoneNew = new ResourceLocation(stoneNew.getNamespace(), stoneNew.getPath().replaceFirst("textures/","").replaceFirst("\\.png",""));
        ore = new ResourceLocation(ore.getNamespace(), ore.getPath().replaceFirst("textures/","").replaceFirst("\\.png",""));
        var p1 = new DynamicTextureSource(out, new AnimationSplittingSource(Map.of(
                "ore", new AnimationSplittingSource.TimeAwareSource(new TextureReader(ore),1),
                "stoneOrig", new AnimationSplittingSource.TimeAwareSource(new TextureReader(stoneOrig),1),
                "stoneNew", new AnimationSplittingSource.TimeAwareSource(new TextureReader(stoneNew),1)),
                new ForegroundTransfer(
                        new AnimationFrameCapture("stoneOrig"),
                        new AnimationFrameCapture("ore"),
                        new AnimationFrameCapture("stoneNew"),
                        6,
                        true,
                        true,
                        true,
                        0.2)));

        var p2 = new TextureMetaGenerator(
                List.of(stoneOrig,ore,stoneNew),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                out);
        return new Pair<>(p1,p2);
    }

    @Override
    public IPathAwareInputStreamSource get() {
        Map<ResourceLocation,Supplier<InputStream>> resources = BlockStateAssembler.getMap(this, this.originalPairs, this.toMake);
        return new IPathAwareInputStreamSource() {
            @Override
            public @NotNull Set<ResourceLocation> getLocations() {
                return resources.keySet();
            }

            @Override
            public @NotNull Supplier<InputStream> get(ResourceLocation outRl) {
                return resources.getOrDefault(outRl, ()->null);
            }
        };
    }
}
