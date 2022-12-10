package dev.lukebemish.excavatedvariants.data.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Optional;

public record BlockProps(Optional<Float> destroyTime, Optional<Float> explosionResistance,
                         Optional<IntProvider> xpDropped) {
    public static final Codec<BlockProps> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("destroy_time").forGetter(BlockProps::destroyTime),
            Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(BlockProps::explosionResistance),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("xp").forGetter(BlockProps::xpDropped)
    ).apply(i, BlockProps::new));
}
