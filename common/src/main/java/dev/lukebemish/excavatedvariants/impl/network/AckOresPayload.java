/*
 * Copyright (C) 2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.network;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record AckOresPayload() {
    public static final ResourceLocation ID = ExcavatedVariants.id("ack_ores");
    public static AckOresPayload decode(FriendlyByteBuf buffer) {
        return new AckOresPayload();
    }

    public void encode(FriendlyByteBuf buffer) {}
}
