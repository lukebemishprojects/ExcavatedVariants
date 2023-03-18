package dev.lukebemish.excavatedvariants.data.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.data.BaseOre;

public sealed interface StringHeldFilter extends Filter {
    Codec<StringHeldFilter> CODEC = Codec.STRING.flatXmap(StringHeldFilter::of, f -> DataResult.success(f.toString()));

    static DataResult<StringHeldFilter> of(String part) {
        if (part.equals("*"))
            return DataResult.success(AllFilter.INSTANCE);
        else if (part.equals("~"))
            return DataResult.success(EmptyFilter.INSTANCE);
        else if (part.contains(":")) {
            if (part.startsWith("stone:"))
                return DataResult.success(new StoneFilter(part.replaceFirst("stone:", "")));
            else if (part.startsWith("ore:"))
                return DataResult.success(new OreFilter(part.replaceFirst("ore:", "")));
            else if (part.startsWith("type:"))
                return DataResult.success(new TypeFilter(part.replaceFirst("type:", "")));
            else
                return DataResult.error(() -> "Unknown filter type '" + part.split(":")[0] + "'");
        } else if (part.startsWith("~")) {
            DataResult<StringHeldFilter> wrapper = of(part.replaceFirst("!", ""));
            return wrapper.map(NotFilter::new);
        } else
            return DataResult.success(new VariantFilter(part));
    }

    record StoneFilter(String stone) implements StringHeldFilter {
        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return matches(ore.id, stone.id);
        }

        @Override
        public boolean matches(String ore, String stone) {
            return stone.equals(stone());
        }

        @Override
        public String toString() {
            return "stone:" + stone();
        }
    }

    record OreFilter(String ore) implements StringHeldFilter {
        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return matches(ore.id, stone.id);
        }

        @Override
        public boolean matches(String ore, String stone) {
            return ore.equals(ore());
        }

        @Override
        public String toString() {
            return "ore:" + ore();
        }
    }

    record VariantFilter(String variant) implements StringHeldFilter {
        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return matches(ore.id, stone.id);
        }

        @Override
        public boolean matches(String ore, String stone) {
            return (stone + "_" + ore).equals(variant());
        }

        @Override
        public String toString() {
            return variant;
        }
    }

    record NotFilter(StringHeldFilter wrapped) implements StringHeldFilter {
        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return wrapped.matches(ore, stone);
        }

        @Override
        public boolean matches(String ore, String stone) {
            return !wrapped().matches(ore, stone);
        }

        @Override
        public String toString() {
            return "!" + wrapped().toString();
        }
    }

    record TypeFilter(String type) implements StringHeldFilter {
        @Override
        public boolean matches(String ore, String stone) {
            if (ExcavatedVariants.getOres() == null || ExcavatedVariants.getStones() == null) {
                ExcavatedVariants.LOGGER.error("Attempted to access type filter based on string prior to config setup. " +
                        "This should not happen, and is *not good*. Unintended behavior may result. Please report to Excavated Variants.");
                return false;
            }
            return ExcavatedVariants.getOres().get(ore).types.contains(type())
                    && ExcavatedVariants.getStones().get(stone).types.contains(type());
        }

        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return ore.types.contains(type()) && stone.types.contains(type());
        }

        @Override
        public String toString() {
            return "type:" + type;
        }
    }

    final class AllFilter implements StringHeldFilter {
        public static final AllFilter INSTANCE = new AllFilter();

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
        public String toString() {
            return "*";
        }
    }

    final class EmptyFilter implements StringHeldFilter {
        public static final EmptyFilter INSTANCE = new EmptyFilter();

        private EmptyFilter() {
        }

        @Override
        public boolean matches(BaseOre ore, BaseStone stone) {
            return false;
        }

        @Override
        public boolean matches(String ore, String stone) {
            return false;
        }

        @Override
        public String toString() {
            return "~";
        }
    }
}
