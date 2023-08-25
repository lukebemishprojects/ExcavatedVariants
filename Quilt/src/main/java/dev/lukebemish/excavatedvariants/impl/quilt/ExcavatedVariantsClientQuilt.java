/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariantsClient;
import dev.lukebemish.excavatedvariants.impl.S2CConfigAgreementPacket;
import net.minecraft.network.FriendlyByteBuf;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;

import java.util.concurrent.CompletableFuture;

public class ExcavatedVariantsClientQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer container) {
        ExcavatedVariantsClient.init();

        ClientLoginNetworking.registerGlobalReceiver(ExcavatedVariantsQuilt.S2C_CONFIG_AGREEMENT_PACKET, ((client, handler, buf, listenerAdder) -> {
            S2CConfigAgreementPacket msg = S2CConfigAgreementPacket.decoder(buf);
            msg.consumeMessage(string -> handler.handleDisconnect(new S2CConfigAgreementPacket.ExcavatedVariantsDisconnectPacket(string)));
            CompletableFuture<FriendlyByteBuf> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }));
    }
}
