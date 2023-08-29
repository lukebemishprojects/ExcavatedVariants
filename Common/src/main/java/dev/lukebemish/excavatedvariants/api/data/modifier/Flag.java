/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Flags that can be applied to variants.
 * <p>
 * <b>Note that the values of this enum are not considered to be an API surface; more flags may be added at any time.</b>
 */
public enum Flag implements StringRepresentable {
    /**
     * The variant should drop whatever its original block would drop, unless silk touch is used. Note that this flag
     * is irrelevant for any ore blocks that have the behaviour of dropping "raw ore chunks" or the like, and is only
     * relevant for when the original block drops itself.
     */
    ORIGINAL_WITHOUT_SILK,
    /**
     * The variant should always drop whatever its original block would drop, even if silk touch is used.
     */
    ORIGINAL_ALWAYS,
    /**
     * The variant should not be created or recognized by the mod at all.
     */
    DISABLE,
    /**
     * The mod is still aware of this variant, but it is not to be used as a parent to generate new variants.
     */
    NON_GENERATING;

    public static final Codec<Flag> CODEC = StringRepresentable.fromEnum(Flag::values);

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
