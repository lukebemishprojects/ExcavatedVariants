package io.github.lukebemish.excavated_variants.data.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum Flag implements StringRepresentable {
    ORIGINAL_WITHOUT_SILK,
    ORIGINAL_ALWAYS;

    public static final Codec<Flag> CODEC = StringRepresentable.fromEnum(Flag::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
