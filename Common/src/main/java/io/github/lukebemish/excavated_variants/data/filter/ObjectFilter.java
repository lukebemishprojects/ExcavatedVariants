package io.github.lukebemish.excavated_variants.data.filter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.function.Function;

public sealed interface ObjectFilter extends Filter {
    BiMap<String, Codec<? extends ObjectFilter>> FILTER_TYPES = new ImmutableBiMap.Builder<String, Codec<? extends ObjectFilter>>()
            .put("and", AndFilter.CODEC)
            .put("or", OrFilter.CODEC)
            .put("not", NotFilter.CODEC)
            .put("empty", EmptyFilter.CODEC)
            .put("all", AllFilter.CODEC)
            .build();
    Codec<ObjectFilter> CODEC = ExtraCodecs.lazyInitializedCodec(() -> new Codec<Codec<? extends ObjectFilter>>() {
        @Override
        public <T> DataResult<Pair<Codec<? extends ObjectFilter>, T>> decode(DynamicOps<T> ops, T input) {
            return Codec.STRING.decode(ops, input).flatMap(keyValuePair -> !FILTER_TYPES.containsKey(keyValuePair.getFirst())
                    ? DataResult.error("Unknown filter type: " + keyValuePair.getFirst())
                    : DataResult.success(keyValuePair.mapFirst(FILTER_TYPES::get)));
        }

        @Override
        public <T> DataResult<T> encode(Codec<? extends ObjectFilter> input, DynamicOps<T> ops, T prefix) {
            String key = FILTER_TYPES.inverse().get(input);
            if (key == null) {
                return DataResult.error("Unregistered filter type: " + input);
            }
            T toMerge = ops.createString(key);
            return ops.mergeToPrimitive(prefix, toMerge);
        }
    }).dispatch(ObjectFilter::codec, Function.identity());

    Codec<? extends ObjectFilter> codec();

    final class AllFilter implements ObjectFilter {
        public static final AllFilter INSTANCE = new AllFilter();
        public static final Codec<AllFilter> CODEC = Codec.unit(INSTANCE);
        private AllFilter() {
        }

        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return true;
        }

        @Override
        public boolean matches(String ore, String stone) {
            return true;
        }

        @Override
        public Codec<? extends ObjectFilter> codec() {
            return CODEC;
        }
    }

    record AndFilter(List<Filter> filters) implements ObjectFilter {
        public static final Codec<AndFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                Filter.CODEC.listOf().fieldOf("filters").forGetter(AndFilter::filters)
        ).apply(i, AndFilter::new));


        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return filters.stream().allMatch(f -> f.matches(ore, stone));
        }

        @Override
        public boolean matches(String ore, String stone) {
            return filters.stream().allMatch(f -> f.matches(ore, stone));
        }

        @Override
        public Codec<? extends ObjectFilter> codec() {
            return CODEC;
        }
    }

    final class EmptyFilter implements ObjectFilter {
        public static final EmptyFilter INSTANCE = new EmptyFilter();
        public static final Codec<EmptyFilter> CODEC = Codec.unit(INSTANCE);
        private EmptyFilter() {
        }

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

    record NotFilter(Filter filter) implements ObjectFilter {
        public static final Codec<NotFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                Filter.CODEC.fieldOf("filter").forGetter(NotFilter::filter)
        ).apply(i, NotFilter::new));

        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return !filter().matches(ore, stone);
        }

        @Override
        public boolean matches(String ore, String stone) {
            return !filter().matches(ore, stone);
        }

        @Override
        public Codec<? extends ObjectFilter> codec() {
            return CODEC;
        }
    }

    record OrFilter(List<Filter> filters) implements ObjectFilter {
        public static final Codec<OrFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                Filter.CODEC.listOf().fieldOf("filters").forGetter(OrFilter::filters)
        ).apply(i, OrFilter::new));


        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return filters.stream().anyMatch(f -> f.matches(ore, stone));
        }

        @Override
        public boolean matches(String ore, String stone) {
            return filters.stream().anyMatch(f -> f.matches(ore, stone));
        }

        @Override
        public Codec<? extends ObjectFilter> codec() {
            return CODEC;
        }
    }
}
