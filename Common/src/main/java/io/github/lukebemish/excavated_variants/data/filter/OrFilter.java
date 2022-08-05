package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record OrFilter(List<Filter> filters) implements ObjectFilter {
    public static final Codec<OrFilter> CODEC = RecordCodecBuilder.create(i->i.group(
            Filter.CODEC.listOf().fieldOf("filters").forGetter(OrFilter::filters)
    ).apply(i,OrFilter::new));


    @Override
    public boolean matches(String ore, String stone) {
        return filters.stream().anyMatch(f->f.matches(ore,stone));
    }

    @Override
    public Codec<? extends ObjectFilter> codec() {
        return CODEC;
    }
}
