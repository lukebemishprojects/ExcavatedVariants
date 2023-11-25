/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.data.filter.ObjectVariantFilter;
import dev.lukebemish.excavatedvariants.impl.data.filter.StringHeldVariantFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public interface VariantFilter {
    Codec<VariantFilter> CODEC = ExtraCodecs.lazyInitializedCodec(() -> Codec.either(StringHeldVariantFilter.CODEC, ObjectVariantFilter.CODEC)
            .flatXmap(e -> e.map(DataResult::success, DataResult::success), (VariantFilter f) -> {
        if (f instanceof StringHeldVariantFilter single)
            return DataResult.success(Either.left(single));
        if (f instanceof ObjectVariantFilter objectFilter)
            return DataResult.success(Either.right(objectFilter));
        return DataResult.error(() -> "Not a serializable filter: " + f);
    }));

    boolean matches(Ore ore, Stone stone, ResourceLocation block);
}
