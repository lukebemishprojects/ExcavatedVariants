package io.github.lukebemish.excavated_variants.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public sealed interface SingleFilter extends Filter {
    Codec<SingleFilter> CODEC = Codec.STRING.flatXmap(SingleFilter::of, f->DataResult.success(f.toString()));

    static DataResult<SingleFilter> of(String part) {
        if (part.contains(":")) {
            if (part.startsWith("stone:")) {
                return DataResult.success(new StoneFilter(part.replaceFirst("stone:","")));
            } else if (part.startsWith("ore:")) {
                return DataResult.success(new OreFilter(part.replaceFirst("ore:","")));
            } else {
                return DataResult.error("Unknown filter type '"+ part.split(":")[0] +"'");
            }
        } else if (part.startsWith("~")) {
            DataResult<SingleFilter> wrapper = of(part.replaceFirst("!",""));
            return wrapper.map(NotFilter::new);
        } else {
            return DataResult.success(new VariantFilter(part));
        }
    }

    record StoneFilter(String stone) implements SingleFilter {
        @Override
        public boolean matches(String ore, String stone) {
            return stone.equals(stone());
        }

        @Override
        public String toString() {
            return "stone:"+stone();
        }
    }

    record OreFilter(String ore) implements SingleFilter {
        @Override
        public boolean matches(String ore, String stone) {
            return ore.equals(ore());
        }

        @Override
        public String toString() {
            return "ore:"+ore();
        }
    }

    record VariantFilter(String variant) implements SingleFilter {
        @Override
        public boolean matches(String ore, String stone) {
            return (stone+"_"+ore).equals(variant());
        }

        @Override
        public String toString() {
            return variant;
        }
    }

    record NotFilter(SingleFilter wrapped) implements SingleFilter {
        @Override
        public boolean matches(String ore, String stone) {
            return !wrapped().matches(ore, stone);
        }

        @Override
        public String toString() {
            return "!"+wrapped().toString();
        }
    }
}
