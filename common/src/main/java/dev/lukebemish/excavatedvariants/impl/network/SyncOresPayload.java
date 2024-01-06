/*
 * Copyright (C) 2023-2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.network;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record SyncOresPayload(Set<String> blocks) {
    public static final ResourceLocation ID = ExcavatedVariants.id("sync_ores");

    public static SyncOresPayload decode(FriendlyByteBuf buffer) {
        ArrayList<String> blocks = new ArrayList<>();
        int i = buffer.readInt();
        for (int j = 0; j < i; j++) blocks.add(buffer.readUtf());
        return new SyncOresPayload(new HashSet<>(blocks));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(blocks.size());
        blocks.forEach(buffer::writeUtf);
    }

    public void consumeMessage(Consumer<String> disconnecter) {
        ExcavatedVariants.setupMap();
        Set<String> knownBlocks = ExcavatedVariants.COMPLETE_VARIANTS.stream().map(v -> v.fullId).collect(Collectors.toSet());
        var serverOnly = this.blocks.stream().filter(b -> !knownBlocks.contains(b)).collect(Collectors.toSet());
        var clientOnly = knownBlocks.stream().filter(b -> !this.blocks.contains(b)).collect(Collectors.toSet());

        if (clientOnly.isEmpty() && serverOnly.isEmpty()) {
            return;
        }
        String disconnect = "Connection closed - mismatched ore variant list";
        if (!clientOnly.isEmpty()) {
            String clientOnlyStr = String.join("\n    ", clientOnly.stream().toList());
            ExcavatedVariants.LOGGER.error("Client contains ore variants not present on server:\n    {}", clientOnlyStr);
            disconnect += "\nSee log for details";
        }
        if (!serverOnly.isEmpty()) {
            String serverOnlyStr = String.join("\n    ", serverOnly.stream().toList());
            ExcavatedVariants.LOGGER.error("Server contains ore variants not present on client:\n    {}", serverOnlyStr);
            disconnect += "\nSee log for details";
        }
        disconnecter.accept(disconnect);
    }
}
