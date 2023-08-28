/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;


import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.S2CConfigAgreementPacket;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFinderUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerNegotiationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.List;
import java.util.stream.Collectors;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Ore Gen
        OreFinderUtil.setupBlocks();
    }

    @SubscribeEvent
    public static void onPlayerNegotiation(PlayerNegotiationEvent playerNegotiationEvent) {
        EVPacketHandler.INSTANCE.sendTo(
                new S2CConfigAgreementPacket(
                        ExcavatedVariants.COMPLETE_VARIANTS.stream().map(v -> v.fullId).collect(Collectors.toSet())
                ),
                playerNegotiationEvent.getConnection(),
                NetworkDirection.LOGIN_TO_CLIENT
        );
    }


    @SubscribeEvent
    public static void mapMissingVariants(MissingMappingsEvent missingMappingsEvent) {
        missingMappingsEvent.getAllMappings(ForgeRegistries.Keys.ITEMS).forEach(EventHandler::remap);
        missingMappingsEvent.getAllMappings(ForgeRegistries.Keys.BLOCKS).forEach(EventHandler::remap);
    }


    private static <T> void remap(MissingMappingsEvent.Mapping<T> mapping) {
        if (mapping.getKey().getNamespace().equals(ExcavatedVariants.MOD_ID)) {
            List<ResourceLocation> locations = ExcavatedVariants.MAPPINGS_CACHE.get(mapping.getKey().getPath());
            T target = null;
            for (ResourceLocation location : locations) {
                T value = mapping.getRegistry().getValue(location);
                if (value != null) {
                    target = value;
                    break;
                }
            }
            if (target != null) {
                mapping.remap(target);
            }
        }
    }
}
