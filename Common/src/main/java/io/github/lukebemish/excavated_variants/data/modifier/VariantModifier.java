package io.github.lukebemish.excavated_variants.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.data.filter.Filter;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.List;
import java.util.Optional;

public record VariantModifier(Filter filter, Optional<Float> destroyTime, Optional<Float> explosionResistance,
                              Optional<IntProvider> xpDropped, Optional<List<Flag>> flags) {
    public static final Codec<VariantModifier> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Filter.CODEC.fieldOf("filter").forGetter(vm -> vm.filter),
            Codec.FLOAT.optionalFieldOf("destroy_time").forGetter(vm -> vm.destroyTime),
            Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(vm -> vm.explosionResistance),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("xp").forGetter(vm -> vm.xpDropped),
            Flag.CODEC.listOf().optionalFieldOf("flags").forGetter(VariantModifier::flags)
    ).apply(instance, VariantModifier::new));
}
