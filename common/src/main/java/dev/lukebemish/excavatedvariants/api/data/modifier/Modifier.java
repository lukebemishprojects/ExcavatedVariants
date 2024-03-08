/*
 * Copyright (C) 2023-2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.data.filter.VariantFilter;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.impl.data.modifier.BlockPropsModifierImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Modifier {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariantFilter.CODEC.fieldOf("filter").forGetter(m -> m.variantFilter),
            BlockPropsModifierImpl.CODEC.<BlockPropsModifier>flatXmap(DataResult::success, p -> {
                if (p instanceof BlockPropsModifierImpl impl)
                    return DataResult.success(impl);
                return DataResult.error(() -> "Not a serializable modifier: " + p);
            }).optionalFieldOf("properties").forGetter(m -> Optional.ofNullable(m.properties)),
            Flag.CODEC.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("flags", Set.of()).forGetter(m -> m.flags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(m -> m.tags)
    ).apply(instance, (filter, properties, flags, tags) -> new Modifier(filter, properties.orElse(null), tags, flags)));

    public final VariantFilter variantFilter;
    public final @Nullable BlockPropsModifier properties;
    public final List<ResourceLocation> tags;
    public final Set<Flag> flags;

    public Modifier(VariantFilter variantFilter, @Nullable BlockPropsModifier properties, List<ResourceLocation> tags, Set<Flag> flags) {
        this.variantFilter = variantFilter;
        this.properties = properties;
        this.tags = tags;
        this.flags = flags;
    }

    public Holder<Modifier> getHolder() {
        return RegistriesImpl.MODIFIER_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<Modifier> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered modifier"));
    }

    public static class Builder {
        private @Nullable VariantFilter variantFilter;
        private @Nullable BlockPropsModifier properties;
        private final List<ResourceLocation> tags = new ArrayList<>();
        private final Set<Flag> flags = new HashSet<>();

        /**
         * @param variantFilter a filter that determines which variants this modifier applies to
         */
        public Builder setVariantFilter(VariantFilter variantFilter) {
            this.variantFilter = variantFilter;
            return this;
        }

        /**
         * @param properties an optional modifier for block properties of generated variants
         */
        public Builder setProperties(BlockPropsModifier properties) {
            this.properties = properties;
            return this;
        }

        /**
         * @param tags tags that should be applied to generated variants
         */
        public Builder setTags(List<ResourceLocation> tags) {
            this.tags.clear();
            this.tags.addAll(tags);
            return this;
        }

        /**
         * @param tag a tag that should be applied to generated variants
         */
        public Builder addTag(ResourceLocation tag) {
            this.tags.add(tag);
            return this;
        }

        /**
         * @param flags flags that should be applied to generated variants
         */
        public Builder setFlags(Collection<Flag> flags) {
            this.flags.clear();
            this.flags.addAll(flags);
            return this;
        }

        /**
         * @param flag a flag that should be applied to generated variants
         */
        public Builder addFlag(Flag flag) {
            this.flags.add(flag);
            return this;
        }

        public Modifier build() {
            Objects.requireNonNull(this.variantFilter);
            return new Modifier(variantFilter, properties, tags, flags);
        }
    }
}
