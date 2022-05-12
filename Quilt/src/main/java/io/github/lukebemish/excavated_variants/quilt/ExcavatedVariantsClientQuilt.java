package io.github.lukebemish.excavated_variants.quilt;

import io.github.lukebemish.excavated_variants.ExcavatedVariantsClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ExcavatedVariantsClientQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer container) {
        ExcavatedVariantsClient.init();
    }
}
