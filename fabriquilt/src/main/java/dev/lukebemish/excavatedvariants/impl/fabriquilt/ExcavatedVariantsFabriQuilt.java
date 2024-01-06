/*
 * Copyright (C) 2023-2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import dev.lukebemish.excavatedvariants.impl.*;
import dev.lukebemish.excavatedvariants.impl.network.SyncOresPayload;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFinderUtil;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreReplacer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.stream.Collectors;

public class ExcavatedVariantsFabriQuilt {
    public static void onInitialize() {
        ExcavatedVariants.init();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ExcavatedVariantsClient.init();
        }

        BuiltInRegistries.BLOCK.holders().forEach(reference ->
                BlockAddedCallback.onRegister(reference.value(), reference.key()));
        BlockAddedCallback.setReady();
        BlockAddedCallback.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                OreFinderUtil.setupBlocks());

        ResourceKey<PlacedFeature> confKey = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"));
        BiomeModifications.create(confKey.location()).add(ModificationPhase.POST_PROCESSING, (x) -> true, context ->
                context.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, confKey));

        /*if (QuiltLoader.isModLoaded("unearthed") && ExcavatedVariants.setupMap()) {
            HyleCompat.init();
        }*/

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            var packet = new SyncOresPayload(ExcavatedVariants.COMPLETE_VARIANTS.stream().map(v -> v.fullId).collect(Collectors.toSet()));
            ServerConfigurationNetworking.send(handler, new SyncFabricPacket(packet));
        });

        Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), new OreReplacer());
    }

    public static void cleanup() {
        StateCapturer.checkState();
        RegistriesImpl.registerRegistries();
        ExcavatedVariants.initPostRegister();
    }

    public record SyncFabricPacket(SyncOresPayload payload) implements FabricPacket {
        public static PacketType<?> TYPE = PacketType.create(SyncOresPayload.ID, buf -> new SyncFabricPacket(SyncOresPayload.decode(buf)));

        @Override
        public void write(FriendlyByteBuf buf) {
            payload.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }
}
