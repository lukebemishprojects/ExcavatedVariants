package dev.lukebemish.excavatedvariants.impl.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.data.filter.Filter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record VariantModifier(Filter filter, Optional<BlockProps> properties, Optional<List<Flag>> flags,
                              Optional<List<ResourceLocation>> tags) {
    public static final Codec<VariantModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Filter.CODEC.fieldOf("filter").forGetter(VariantModifier::filter),
            BlockProps.CODEC.optionalFieldOf("properties").forGetter(VariantModifier::properties),
            Flag.CODEC.listOf().optionalFieldOf("flags").forGetter(VariantModifier::flags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags").forGetter(VariantModifier::tags)
    ).apply(instance, VariantModifier::new));
}
