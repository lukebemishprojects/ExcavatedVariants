package dev.lukebemish.excavatedvariants.impl.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.data.filter.VariantFilter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class Modifier {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariantFilter.CODEC.fieldOf("filter").forGetter(m -> m.variantFilter),
            BlockPropsModifierImpl.CODEC.<BlockPropsModifier>flatXmap(DataResult::success, p -> {
                if (p instanceof BlockPropsModifierImpl impl)
                    return DataResult.success(impl);
                return DataResult.error(() -> "Not a serializable modifier: " + p);
            }).optionalFieldOf("properties").forGetter(m -> Optional.ofNullable(m.properties)),
            Flag.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(m -> m.flags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(m -> m.tags)
    ).apply(instance, (filter, properties, flags, tags) -> new Modifier(filter, properties.orElse(null), tags, flags)));

    public final VariantFilter variantFilter;
    public final BlockPropsModifier properties;
    public final List<ResourceLocation> tags;
    public final List<Flag> flags;

    public Modifier(VariantFilter variantFilter, BlockPropsModifier properties, List<ResourceLocation> tags, List<Flag> flags) {
        this.variantFilter = variantFilter;
        this.properties = properties;
        this.tags = tags;
        this.flags = flags;
    }
}
