package io.github.lukebemish.excavated_variants.data.filter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ExtraCodecs;

import java.util.function.Function;

public sealed interface ObjectFilter extends Filter permits AllFilter, AndFilter, EmptyFilter, NotFilter, OrFilter {
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
            if (key == null)
            {
                return DataResult.error("Unregistered filter type: " + input);
            }
            T toMerge = ops.createString(key);
            return ops.mergeToPrimitive(prefix, toMerge);
        }
    }).dispatch(ObjectFilter::codec, Function.identity());

    BiMap<String, Codec<? extends ObjectFilter>> FILTER_TYPES = new ImmutableBiMap.Builder<String, Codec<? extends ObjectFilter>>()
            .put("and",AndFilter.CODEC)
            .put("or",OrFilter.CODEC)
            .put("not",NotFilter.CODEC)
            .put("empty",EmptyFilter.CODEC)
            .put("all",AllFilter.CODEC)
            .build();

    Codec<? extends ObjectFilter> codec();
}
