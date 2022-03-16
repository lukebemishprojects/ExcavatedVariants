package com.github.lukebemish.excavated_variants.fabric;

import com.github.lukebemish.excavated_variants.ExcavatedVariantsClient;
import net.fabricmc.api.ClientModInitializer;

public class ExcavatedVariantsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExcavatedVariantsClient.init();
    }
}
