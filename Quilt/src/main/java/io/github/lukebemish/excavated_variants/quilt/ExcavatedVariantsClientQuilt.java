package io.github.lukebemish.excavated_variants.quilt;

import io.github.lukebemish.excavated_variants.ExcavatedVariantsClient;
import io.github.lukebemish.excavated_variants.S2CConfigAgreementPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
            msg.consumeMessage(string -> handler.getConnection().disconnect(Component.literal(string)));
            CompletableFuture<FriendlyByteBuf> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }));
    }
}
