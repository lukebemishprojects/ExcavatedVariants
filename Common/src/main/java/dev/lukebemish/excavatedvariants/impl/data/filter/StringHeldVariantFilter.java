/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.filter.VariantFilter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public sealed interface StringHeldVariantFilter extends VariantFilter {
    Codec<StringHeldVariantFilter> CODEC = Codec.STRING.flatXmap(StringHeldVariantFilter::of, f -> DataResult.success(f.toString()));

    static DataResult<StringHeldVariantFilter> of(String part) {
        if (part.equals("*"))
            return DataResult.success(AllVariantFilter.INSTANCE);
        else if (part.equals("~"))
            return DataResult.success(EmptyVariantFilter.INSTANCE);
        else if (part.equals("generated"))
            return DataResult.success(GeneratedVariantFilter.INSTANCE);
        else if (part.contains("@")) {
            if (part.startsWith("stone@"))
                return ResourceLocation.read(part.replaceFirst("stone@", "")).map(rl -> new StoneVariantFilter(ResourceKey.create(RegistryKeys.STONE, rl)));
            else if (part.startsWith("ore@"))
                return ResourceLocation.read(part.replaceFirst("ore@", "")).map(rl -> new OreVariantFilter(ResourceKey.create(RegistryKeys.ORE, rl)));
            else if (part.startsWith("ground_type@"))
                return ResourceLocation.read(part.replaceFirst("ground_type@", "")).map(rl -> new GroundTypeVariantFilter(ResourceKey.create(RegistryKeys.GROUND_TYPE, rl)));
            else
                return DataResult.error(() -> "Unknown filter type '" + part.split("@")[0] + "'");
        } else if (part.startsWith("~")) {
            DataResult<StringHeldVariantFilter> wrapper = of(part.replaceFirst("~", ""));
            return wrapper.map(NotVariantFilter::new);
        } else
            return DataResult.error(() -> "Note a filter: '" + part + "'");
    }

    record StoneVariantFilter(ResourceKey<Stone> stone) implements StringHeldVariantFilter {
        @Override
        public boolean matches(Ore ore, Stone stone) {
            return stone.getKeyOrThrow() == stone();
        }

        @Override
        public String toString() {
            return "stone@" + stone();
        }
    }

    record OreVariantFilter(ResourceKey<Ore> ore) implements StringHeldVariantFilter {
        @Override
        public boolean matches(Ore ore, Stone stone) {
            return ore.getKeyOrThrow() == ore();
        }

        @Override
        public String toString() {
            return "ore@" + ore();
        }
    }

    record NotVariantFilter(StringHeldVariantFilter wrapped) implements StringHeldVariantFilter {
        @Override
        public boolean matches(Ore ore, Stone stone) {
            return !wrapped.matches(ore, stone);
        }

        @Override
        public String toString() {
            return "~" + wrapped().toString();
        }
    }

    record GroundTypeVariantFilter(ResourceKey<GroundType> type) implements StringHeldVariantFilter {

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return ore.types.contains(type()) && stone.types.contains(type());
        }

        @Override
        public String toString() {
            return "type@" + type;
        }
    }

    final class AllVariantFilter implements StringHeldVariantFilter {
        public static final AllVariantFilter INSTANCE = new AllVariantFilter();

        private AllVariantFilter() {
        }

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return true;
        }

        @Override
        public String toString() {
            return "*";
        }
    }

    final class EmptyVariantFilter implements StringHeldVariantFilter {
        public static final EmptyVariantFilter INSTANCE = new EmptyVariantFilter();

        private EmptyVariantFilter() {
        }

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return false;
        }

        @Override
        public String toString() {
            return "~";
        }
    }

    final class GeneratedVariantFilter implements StringHeldVariantFilter {
        public static final GeneratedVariantFilter INSTANCE = new GeneratedVariantFilter();

        @Override
        public boolean matches(Ore ore, Stone stone) {
            return ore.getOriginalStoneBlocks().get(stone.getKeyOrThrow()) == null;
        }

        @Override
        public String toString() {
            return "generated";
        }
    }
}
