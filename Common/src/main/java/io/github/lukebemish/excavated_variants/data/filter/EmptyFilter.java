package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

public final class EmptyFilter implements ObjectFilter {
    private EmptyFilter() {}
    public static final EmptyFilter INSTANCE = new EmptyFilter();
    public static final Codec<EmptyFilter> CODEC = Codec.unit(INSTANCE);

    @Override
    public boolean matches(String ore, String stone) {
        return false;
    }

    @Override
    public boolean matches(BaseOre ore, BaseStone stone) {
        return false;
    }

    @Override
    public Codec<? extends ObjectFilter> codec() {
        return CODEC;
    }
}
