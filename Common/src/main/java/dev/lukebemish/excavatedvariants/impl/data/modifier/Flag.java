package dev.lukebemish.excavatedvariants.impl.data.modifier;

import java.util.Locale;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public enum Flag implements StringRepresentable {
    ORIGINAL_WITHOUT_SILK,
    ORIGINAL_ALWAYS;

    public static final Codec<Flag> CODEC = StringRepresentable.fromEnum(Flag::values);

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
