package dev.lukebemish.excavatedvariants.impl.data.modifier;

import net.minecraft.util.valueproviders.IntProvider;

import java.util.function.Consumer;

public interface BlockPropsModifier {
    default void setDestroyTime(Consumer<Float> consumer) {}
    default void setExplosionResistance(Consumer<Float> consumer) {}
    default void setXpDropped(Consumer<IntProvider> consumer) {}
}
