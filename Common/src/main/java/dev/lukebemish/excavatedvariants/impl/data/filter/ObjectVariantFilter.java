/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data.filter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.filter.VariantFilter;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.function.Function;

public sealed interface ObjectVariantFilter extends VariantFilter {
    BiMap<String, Codec<? extends ObjectVariantFilter>> FILTER_TYPES = new ImmutableBiMap.Builder<String, Codec<? extends ObjectVariantFilter>>()
            .put("and", AndVariantFilter.CODEC)
            .put("or", OrVariantFilter.CODEC)
            .put("not", NotVariantFilter.CODEC)
            .put("empty", EmptyVariantFilter.CODEC)
            .put("all", AllVariantFilter.CODEC)
            .build();
    Codec<ObjectVariantFilter> CODEC = ExtraCodecs.lazyInitializedCodec(() -> new Codec<Codec<? extends ObjectVariantFilter>>() {
        @Override
        public <T> DataResult<Pair<Codec<? extends ObjectVariantFilter>, T>> decode(DynamicOps<T> ops, T input) {
            return Codec.STRING.decode(ops, input).flatMap(keyValuePair -> !FILTER_TYPES.containsKey(keyValuePair.getFirst())
                    ? DataResult.error(() -> "Unknown filter type: " + keyValuePair.getFirst())
                    : DataResult.success(keyValuePair.mapFirst(FILTER_TYPES::get)));
        }

        @Override
        public <T> DataResult<T> encode(Codec<? extends ObjectVariantFilter> input, DynamicOps<T> ops, T prefix) {
            String key = FILTER_TYPES.inverse().get(input);
            if (key == null) {
                return DataResult.error(() -> "Unregistered filter type: " + input);
            }
            T toMerge = ops.createString(key);
            return ops.mergeToPrimitive(prefix, toMerge);
        }
    }).dispatch(ObjectVariantFilter::codec, Function.identity());

    Codec<? extends ObjectVariantFilter> codec();

    final class AllVariantFilter implements ObjectVariantFilter {
        public static final AllVariantFilter INSTANCE = new AllVariantFilter();
        public static final Codec<AllVariantFilter> CODEC = Codec.unit(INSTANCE);
        private AllVariantFilter() {
        }

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return true;
        }

        @Override
        public Codec<? extends ObjectVariantFilter> codec() {
            return CODEC;
        }
    }

    record AndVariantFilter(List<VariantFilter> variantFilters) implements ObjectVariantFilter {
        public static final Codec<AndVariantFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                VariantFilter.CODEC.listOf().fieldOf("filters").forGetter(AndVariantFilter::variantFilters)
        ).apply(i, AndVariantFilter::new));


        @Override
        public boolean matches(Ore ore, Stone stone) {
            return variantFilters.stream().allMatch(f -> f.matches(ore, stone));
        }

        @Override
        public Codec<? extends ObjectVariantFilter> codec() {
            return CODEC;
        }
    }

    final class EmptyVariantFilter implements ObjectVariantFilter {
        public static final EmptyVariantFilter INSTANCE = new EmptyVariantFilter();
        public static final Codec<EmptyVariantFilter> CODEC = Codec.unit(INSTANCE);
        private EmptyVariantFilter() {
        }

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return false;
        }

        @Override
        public Codec<? extends ObjectVariantFilter> codec() {
            return CODEC;
        }
    }

    record NotVariantFilter(VariantFilter variantFilter) implements ObjectVariantFilter {
        public static final Codec<NotVariantFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                VariantFilter.CODEC.fieldOf("filter").forGetter(NotVariantFilter::variantFilter)
        ).apply(i, NotVariantFilter::new));

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return !variantFilter().matches(ore, stone);
        }

        @Override
        public Codec<? extends ObjectVariantFilter> codec() {
            return CODEC;
        }
    }

    record OrVariantFilter(List<VariantFilter> variantFilters) implements ObjectVariantFilter {
        public static final Codec<OrVariantFilter> CODEC = RecordCodecBuilder.create(i -> i.group(
                VariantFilter.CODEC.listOf().fieldOf("filters").forGetter(OrVariantFilter::variantFilters)
        ).apply(i, OrVariantFilter::new));


        @Override
        public boolean matches(Ore ore, Stone stone) {
            return variantFilters.stream().anyMatch(f -> f.matches(ore, stone));
        }

        @Override
        public Codec<? extends ObjectVariantFilter> codec() {
            return CODEC;
        }
    }
}
