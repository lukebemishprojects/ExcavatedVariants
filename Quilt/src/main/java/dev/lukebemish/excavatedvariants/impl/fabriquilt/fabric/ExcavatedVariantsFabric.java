package dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric;

import dev.lukebemish.excavatedvariants.impl.fabriquilt.ExcavatedVariantsFabriQuilt;
import net.fabricmc.api.ModInitializer;

@SuppressWarnings("deprecation")
public class ExcavatedVariantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExcavatedVariantsFabriQuilt.onInitialize();
    }
}
