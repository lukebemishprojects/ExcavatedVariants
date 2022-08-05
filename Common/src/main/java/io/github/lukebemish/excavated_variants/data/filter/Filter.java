package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.util.ExtraCodecs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public sealed interface Filter permits StringHeldFilter, ObjectFilter {
    Codec<Filter> CODEC = ExtraCodecs.lazyInitializedCodec(() -> Codec.either(StringHeldFilter.CODEC,ObjectFilter.CODEC).xmap(either -> {
        if (either.left().isPresent())
            return either.left().get();
        return either.right().get();
    }, (Filter f) -> {
        if (f instanceof StringHeldFilter single)
            return Either.left(single);
        return Either.right((ObjectFilter) f);
    }));

    boolean matches(BaseOre ore, BaseStone stone);

    boolean matches(String ore, String stone);

    static Filter union(List<Filter> filters) {
        filters = filters.stream().flatMap(Filter::expandOr).toList();
        return new OrFilter(filters);
    }

    static Filter union(Filter... filters) {
        return union(Arrays.asList(filters));
    }

    static Filter intersect(List<Filter> filters) {
        filters = filters.stream().flatMap(Filter::expandAnd).toList();
        return new AndFilter(filters);
    }

    static Filter intersect(Filter... filters) {
        return intersect(Arrays.asList(filters));
    }

    private static Stream<Filter> expandOr(Filter filter) {
        if (filter instanceof OrFilter or)
            return or.filters().stream().flatMap(Filter::expandOr);
        return Stream.of(filter);
    }

    private static Stream<Filter> expandAnd(Filter filter) {
        if (filter instanceof AndFilter and)
            return and.filters().stream().flatMap(Filter::expandAnd);
        return Stream.of(filter);
    }
}
