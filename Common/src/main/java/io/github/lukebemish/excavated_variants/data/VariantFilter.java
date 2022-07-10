package io.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.HashSet;
import java.util.Set;

public class VariantFilter {
    public static final Codec<VariantFilter> CODEC = Codec.STRING.comapFlatMap(VariantFilter::read, VariantFilter::toString).stable();

    private final Set<String> filteredStones = new HashSet<>();
    private final Set<String> filteredOres = new HashSet<>();
    private final Set<String> filteredVariants = new HashSet<>();
    private final Set<String> ignoredVariants = new HashSet<>();

    public static DataResult<VariantFilter> read(String filter) {
        try {
            return DataResult.success(new VariantFilter(filter));
        } catch (VariantFilterException var2) {
            return DataResult.error("Not a valid variant filter: " + filter + " " + var2.getMessage());
        }
    }

    @Override
    public String toString() {
        Set<String> allStrings = new HashSet<>();
        filteredStones.stream().map(s->"stone:"+s).forEach(allStrings::add);
        filteredOres.stream().map(s->"ore:"+s).forEach(allStrings::add);
        allStrings.addAll(filteredVariants);
        ignoredVariants.stream().map(s->"~"+s).forEach(allStrings::add);
        return String.join(",",allStrings);
    }

    public VariantFilter() {

    }

    public VariantFilter(String string) {
        String[] parts = string.split(",");
        for (String part : parts) {
            if (part.contains(":")) {
                if (part.startsWith("stone:")) {
                    filteredStones.add(part.replaceFirst("stone:",""));
                } else if (part.startsWith("ore:")) {
                    filteredOres.add(part.replaceFirst("ore:",""));
                } else {
                    throw new VariantFilterException("Unknown filter '"+ part.split(":")[0] +"'");
                }
            } else if (part.startsWith("~")) {
                ignoredVariants.add(part.replaceFirst("~",""));
            } else {
                filteredVariants.add(part);
            }
        }
    }

    public boolean matches(BaseOre ore, BaseStone stone) {
        return this.matches(ore.id, stone.id);
    }

    public boolean matches(String ore, String stone) {
        String fullId = stone+"_"+ore;
        return (filteredOres.contains(ore) || filteredStones.contains(stone) || filteredVariants.contains(fullId)) && !ignoredVariants.contains(fullId);
    }

    public void mergeFrom(VariantFilter blacklist) {
        this.filteredStones.addAll(blacklist.filteredStones);
        this.filteredOres.addAll(blacklist.filteredOres);
        this.filteredVariants.addAll(blacklist.filteredVariants);
        this.ignoredVariants.addAll(blacklist.ignoredVariants);
    }

    public static class VariantFilterException extends RuntimeException {
        public VariantFilterException(String s) {
            super(s);
        }
    }
}
