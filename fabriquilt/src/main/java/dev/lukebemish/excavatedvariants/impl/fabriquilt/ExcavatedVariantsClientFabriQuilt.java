/*
 * Copyright (C) 2023-2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import dev.lukebemish.excavatedvariants.impl.network.SyncOresPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class ExcavatedVariantsClientFabriQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(SyncOresPayload.ID, ((client, handler, buf, listenerAdder) -> {
            SyncOresPayload msg = SyncOresPayload.decode(buf);
            msg.consumeMessage(string -> handler.handleDisconnect(new ClientboundDisconnectPacket(Component.literal(string))));
        }));
    }
}
