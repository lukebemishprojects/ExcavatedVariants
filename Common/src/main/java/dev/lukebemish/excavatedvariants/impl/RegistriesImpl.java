/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.mojang.serialization.Lifecycle;
import dev.lukebemish.excavatedvariants.api.PostRegistrationListener;
import dev.lukebemish.excavatedvariants.api.RegistrationListener;
import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import dev.lukebemish.excavatedvariants.impl.mixin.BuiltInRegistriesMixin;
import dev.lukebemish.excavatedvariants.impl.mixin.MappedRegistryMixin;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class RegistriesImpl {
    private RegistriesImpl() {}

    public static final ResourceKey<Registry<GroundType>> GROUND_TYPE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(ExcavatedVariants.MOD_ID, "ground_type"));
    public static final ResourceKey<Registry<Ore>> ORE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(ExcavatedVariants.MOD_ID, "ore"));
    public static final ResourceKey<Registry<Stone>> STONE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(ExcavatedVariants.MOD_ID, "stone"));
    public static final ResourceKey<Registry<Modifier>> MODIFIER_KEY = ResourceKey.createRegistryKey(new ResourceLocation(ExcavatedVariants.MOD_ID, "modifier"));

    public static final WritableRegistry<GroundType> GROUND_TYPE_REGISTRY = new MappedRegistry<>(GROUND_TYPE_KEY, Lifecycle.stable(), false);
    public static final WritableRegistry<Ore> ORE_REGISTRY = new MappedRegistry<>(ORE_KEY, Lifecycle.stable(), false);
    public static final WritableRegistry<Stone> STONE_REGISTRY = new MappedRegistry<>(STONE_KEY, Lifecycle.stable(), false);
    public static final WritableRegistry<Modifier> MODIFIER_REGISTRY = new MappedRegistry<>(MODIFIER_KEY, Lifecycle.stable(), false);

    static {
        GROUND_TYPE_REGISTRY.freeze();
        ORE_REGISTRY.freeze();
        STONE_REGISTRY.freeze();
        MODIFIER_REGISTRY.freeze();
    }

    public static void bootstrap() {
        ((MappedRegistryMixin) GROUND_TYPE_REGISTRY).setFrozen(false);
        ((MappedRegistryMixin) ORE_REGISTRY).setFrozen(false);
        ((MappedRegistryMixin) STONE_REGISTRY).setFrozen(false);
        ((MappedRegistryMixin) MODIFIER_REGISTRY).setFrozen(false);

        Map<ResourceLocation, List<Ore>> ores = new HashMap<>();

        BiConsumer<ResourceLocation, GroundType> groundTypes = (l, o) -> Registry.register(GROUND_TYPE_REGISTRY, l, o);
        BiConsumer<ResourceLocation, Stone> stoneTypes = (l, o) -> Registry.register(STONE_REGISTRY, l, o);
        BiConsumer<ResourceLocation, Ore> oreTypes = (l, o) -> ores.computeIfAbsent(l, k -> new ArrayList<>()).add(o);
        BiConsumer<ResourceLocation, Modifier> modifiers = (l, o) -> Registry.register(MODIFIER_REGISTRY, l, o);

        Services.COMPAT.getListeners(RegistrationListener.class).forEach(r -> r.provideEntries(new RegistrationListener.Registrar(groundTypes, stoneTypes, oreTypes, modifiers)));

        ores.forEach((l, os) -> Registry.register(ORE_REGISTRY, l, Ore.merge(os)));

        GROUND_TYPE_REGISTRY.freeze();
        ORE_REGISTRY.freeze();
        STONE_REGISTRY.freeze();
        MODIFIER_REGISTRY.freeze();

        Services.COMPAT.getListeners(PostRegistrationListener.class).forEach(r -> r.registriesComplete(new PostRegistrationListener.Registries(GROUND_TYPE_REGISTRY, STONE_REGISTRY, ORE_REGISTRY, MODIFIER_REGISTRY)));
    }

    public static void registerRegistries() {
        ((MappedRegistryMixin) BuiltInRegistries.REGISTRY).setFrozen(false);
        Registry.register(BuiltInRegistriesMixin.getWritableRegistry(), GROUND_TYPE_KEY.location(), GROUND_TYPE_REGISTRY);
        Registry.register(BuiltInRegistriesMixin.getWritableRegistry(), ORE_KEY.location(), ORE_REGISTRY);
        Registry.register(BuiltInRegistriesMixin.getWritableRegistry(), STONE_KEY.location(), STONE_REGISTRY);
        Registry.register(BuiltInRegistriesMixin.getWritableRegistry(), MODIFIER_KEY.location(), MODIFIER_REGISTRY);
        BuiltInRegistries.REGISTRY.freeze();
    }
}
