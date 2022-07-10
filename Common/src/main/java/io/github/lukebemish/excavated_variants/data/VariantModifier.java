package io.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Optional;

public record VariantModifier(VariantFilter filter, Optional<Float> destroyTime, Optional<Float> explosionResistance,
                              Optional<IntProvider> xpDropped) {
    public static final Codec<VariantModifier> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            VariantFilter.CODEC.fieldOf("filter").forGetter(vm -> vm.filter),
            Codec.FLOAT.optionalFieldOf("destroy_time").forGetter(vm -> vm.destroyTime),
            Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(vm -> vm.explosionResistance),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("xp").forGetter(vm -> vm.xpDropped)
    ).apply(instance, VariantModifier::new));

}
