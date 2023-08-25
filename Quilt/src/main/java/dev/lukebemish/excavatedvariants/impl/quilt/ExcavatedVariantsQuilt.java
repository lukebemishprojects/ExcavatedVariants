/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.impl.S2CConfigAgreementPacket;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFinderUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ExcavatedVariantsQuilt implements ModInitializer {
    public static final ResourceLocation S2C_CONFIG_AGREEMENT_PACKET = new ResourceLocation(ExcavatedVariants.MOD_ID, "config_agreement");

    private boolean isRegistering = false;

    @Override
    public void onInitialize(ModContainer modContainer) {
        ExcavatedVariants.init();
        RegistriesImpl.registerRegistries();

        ExcavatedVariants.loadedBlockRLs.addAll(BuiltInRegistries.BLOCK.keySet());

        ArrayList<ExcavatedVariants.VariantFuture> toRemove = new ArrayList<>();
        for (ExcavatedVariants.VariantFuture b : ExcavatedVariants.getBlockList()) {
            if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.blockId.get(0)) &&
                    ExcavatedVariants.loadedBlockRLs.contains(b.stone.blockId)) {
                ExcavatedVariants.registerBlockAndItem(
                        (rl, bl) -> Registry.register(BuiltInRegistries.BLOCK, rl, bl),
                        (rl, i) -> {
                            Item out = Registry.register(BuiltInRegistries.ITEM, rl, i.get());
                            return () -> out;
                        }, b);
                toRemove.add(b);
            }
        }
        ExcavatedVariants.blockList.removeAll(toRemove);

        RegistryEvents.getEntryAddEvent(BuiltInRegistries.BLOCK).register(ctx -> {
            ResourceLocation rl = ctx.id();
            if (ExcavatedVariants.neededRls.contains(rl)) {
                ExcavatedVariants.loadedBlockRLs.add(rl);
                if (!isRegistering) {
                    isRegistering = true;
                    ArrayList<ExcavatedVariants.VariantFuture> toRemove2 = new ArrayList<>();
                    for (ExcavatedVariants.VariantFuture b : ExcavatedVariants.getBlockList()) {
                        if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.blockId.get(0)) &&
                                ExcavatedVariants.loadedBlockRLs.contains(b.stone.blockId)) {
                            ExcavatedVariants.registerBlockAndItem(
                                    (orl, bl) -> Registry.register(BuiltInRegistries.BLOCK, orl, bl),
                                    (orl, i) -> {
                                        Item out = Registry.register(BuiltInRegistries.ITEM, orl, i.get());
                                        return () -> out;
                                    }, b);
                            toRemove2.add(b);
                        }
                    }
                    ExcavatedVariants.blockList.removeAll(toRemove2);
                    isRegistering = false;
                }
            }
        });

        ServerLifecycleEvents.STARTING.register(server -> {
            //Ore gen map setup
            Services.REGISTRY_UTIL.reset();
            ExcavatedVariants.oreStoneList = null;
            OreFinderUtil.setupBlocks();
            ExcavatedVariants.setupMap();
        });
        ResourceKey<PlacedFeature> confKey = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"));
        BiomeModifications.create(confKey.location()).add(ModificationPhase.POST_PROCESSING, (x) -> true, context ->
                context.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, confKey));

        /*if (QuiltLoader.isModLoaded("unearthed") && ExcavatedVariants.setupMap()) {
            HyleCompat.init();
        }*/

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            var packet = new S2CConfigAgreementPacket(ExcavatedVariants.oreStoneList.stream().flatMap(p -> p.getSecond().stream().map(
                    stone -> stone.id + "_" + p.getFirst().id)).collect(Collectors.toSet()));
            var buf = PacketByteBufs.create();
            packet.encoder(buf);
            sender.sendPacket(sender.createPacket(S2C_CONFIG_AGREEMENT_PACKET, buf));
        });

        ServerLoginNetworking.registerGlobalReceiver(S2C_CONFIG_AGREEMENT_PACKET, ((server, handler, understood, buf, synchronizer, responseSender) -> {
            //Do I need to do anything here?
        }));
    }
}
