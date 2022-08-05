package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

import java.util.List;

public record AndFilter(List<Filter> filters) implements ObjectFilter {
    public static final Codec<AndFilter> CODEC = RecordCodecBuilder.create(i->i.group(
            Filter.CODEC.listOf().fieldOf("filters").forGetter(AndFilter::filters)
    ).apply(i,AndFilter::new));


    @Override
    public boolean matches(BaseOre ore, BaseStone stone) {
        return filters.stream().allMatch(f->f.matches(ore,stone));
    }

    @Override
    public boolean matches(String ore, String stone) {
        return filters.stream().allMatch(f->f.matches(ore,stone));
    }

    @Override
    public Codec<? extends ObjectFilter> codec() {
        return CODEC;
    }
}
