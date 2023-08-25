package dev.lukebemish.excavatedvariants.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class GroundType {
    public static final Codec<GroundType> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("dimension_tag").forGetter(g -> g.dimensionTag)
    ).apply(i, GroundType::new));

    public final ResourceLocation dimensionTag;

    public GroundType(ResourceLocation dimensionTag) {
        this.dimensionTag = dimensionTag;
    }

    public final Holder<GroundType> getHolder() {
        return RegistriesImpl.GROUND_TYPE_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<GroundType> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ground type"));
    }
}
