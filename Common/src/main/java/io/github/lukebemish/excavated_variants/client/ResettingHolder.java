package io.github.lukebemish.excavated_variants.client;

import io.github.lukebemish.dynamic_asset_generator.api.ResettingSupplier;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;

public class ResettingHolder implements ResettingSupplier<InputStream> {
    private final ResourceLocation target;

    public ResettingHolder(ResourceLocation target) {
        this.target = target;
    }

    @Override
    public void reset() {
        BlockStateAssembler.reset();
    }

    @Override
    public InputStream get() {
        var s = BlockStateAssembler.updateMap().get(target);
        if (s!=null) return s.get();
        return null;
    }
}
