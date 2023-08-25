package dev.lukebemish.excavatedvariants.api;

import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.impl.data.GroundType;
import dev.lukebemish.excavatedvariants.impl.data.Ore;
import dev.lukebemish.excavatedvariants.impl.data.Stone;
import dev.lukebemish.excavatedvariants.impl.data.modifier.Modifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class RegistryKeys {
    private RegistryKeys() {}
    public static final ResourceKey<Registry<GroundType>> GROUND_TYPE = RegistriesImpl.GROUND_TYPE_KEY;
    public static final ResourceKey<Registry<Ore>> ORE = RegistriesImpl.ORE_KEY;
    public static final ResourceKey<Registry<Stone>> STONE = RegistriesImpl.STONE_KEY;
    public static final ResourceKey<Registry<Modifier>> MODIFIER = RegistriesImpl.MODIFIER_KEY;
}
