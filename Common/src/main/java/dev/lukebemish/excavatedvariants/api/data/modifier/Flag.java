/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.modifier;

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
