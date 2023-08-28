/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import dev.lukebemish.excavatedvariants.impl.*;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFinderUtil;
import net.fabricmc.api.EnvType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

import java.util.stream.Collectors;

public class ExcavatedVariantsQuilt implements ModInitializer {
    public static final ResourceLocation S2C_CONFIG_AGREEMENT_PACKET = new ResourceLocation(ExcavatedVariants.MOD_ID, "config_agreement");

    private boolean isRegistering = false;

    @Override
    public void onInitialize(ModContainer modContainer) {
        ExcavatedVariants.init();
        if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
            ExcavatedVariantsClient.init();
        }

        BuiltInRegistries.BLOCK.holders().forEach(reference -> {
            BlockAddedCallback.onRegister(reference.value(), reference.key());
        });
        BlockAddedCallback.setReady();
        BlockAddedCallback.register();

        RegistryEvents.getEntryAddEvent(BuiltInRegistries.BLOCK).register(ctx -> {
            BlockAddedCallback.onRegister(ctx.value(), ResourceKey.create(Registries.BLOCK, ctx.id()));
        });

        ServerLifecycleEvents.STARTING.register(server -> {
            OreFinderUtil.setupBlocks();
        });
        ResourceKey<PlacedFeature> confKey = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"));
        BiomeModifications.create(confKey.location()).add(ModificationPhase.POST_PROCESSING, (x) -> true, context ->
                context.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, confKey));

        /*if (QuiltLoader.isModLoaded("unearthed") && ExcavatedVariants.setupMap()) {
            HyleCompat.init();
        }*/

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            var packet = new S2CConfigAgreementPacket(ExcavatedVariants.COMPLETE_VARIANTS.stream().map(v -> v.fullId).collect(Collectors.toSet()));
            var buf = PacketByteBufs.create();
            packet.encoder(buf);
            sender.sendPacket(sender.createPacket(S2C_CONFIG_AGREEMENT_PACKET, buf));
        });

        ServerLoginNetworking.registerGlobalReceiver(S2C_CONFIG_AGREEMENT_PACKET, ((server, handler, understood, buf, synchronizer, responseSender) -> {
            //Do I need to do anything here?
        }));
    }

    public static void cleanup() {
        StateCapturer.checkState();
        RegistriesImpl.registerRegistries();
        ExcavatedVariants.initPostRegister();
    }
}
