package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;

public final class AllFilter implements ObjectFilter {
    private AllFilter() {}
    public static final AllFilter INSTANCE = new AllFilter();
    public static final Codec<AllFilter> CODEC = Codec.unit(INSTANCE);

    @Override
    public boolean matches(String ore, String stone) {
        return true;
    }

    @Override
    public Codec<? extends ObjectFilter> codec() {
        return CODEC;
    }
}
