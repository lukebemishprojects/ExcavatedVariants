/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import dev.lukebemish.excavatedvariants.impl.S2CConfigAgreementPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class ExcavatedVariantsClientFabriQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(ExcavatedVariantsFabriQuilt.S2C_CONFIG_AGREEMENT_PACKET, ((client, handler, buf, listenerAdder) -> {
            S2CConfigAgreementPacket msg = S2CConfigAgreementPacket.decoder(buf);
            msg.consumeMessage(string -> handler.handleDisconnect(new S2CConfigAgreementPacket.ExcavatedVariantsDisconnectPacket(string)));
            CompletableFuture<FriendlyByteBuf> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }));
    }
}
