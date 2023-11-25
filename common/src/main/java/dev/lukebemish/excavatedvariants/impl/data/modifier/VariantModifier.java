/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.data.filter.VariantFilter;
import dev.lukebemish.excavatedvariants.api.data.modifier.Flag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record VariantModifier(VariantFilter variantFilter, Optional<BlockPropsModifierImpl> properties, Optional<List<Flag>> flags,
                              Optional<List<ResourceLocation>> tags) {
    public static final Codec<VariantModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariantFilter.CODEC.fieldOf("filter").forGetter(VariantModifier::variantFilter),
            BlockPropsModifierImpl.CODEC.optionalFieldOf("properties").forGetter(VariantModifier::properties),
            Flag.CODEC.listOf().optionalFieldOf("flags").forGetter(VariantModifier::flags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags").forGetter(VariantModifier::tags)
    ).apply(instance, VariantModifier::new));


}
