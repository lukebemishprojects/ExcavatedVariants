package io.github.lukebemish.excavated_variants.forge;


import io.github.lukebemish.excavated_variants.BiomeInjector;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.S2CConfigAgreementPacket;
import io.github.lukebemish.excavated_variants.mixin.IMinecraftServerMixin;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.worldgen.OreFinderUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerNegotiationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.stream.Collectors;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Ore Gen
        Services.REGISTRY_UTIL.reset();
        ExcavatedVariants.oreStoneList = null;
        OreFinderUtil.setupBlocks();
        ExcavatedVariants.setupMap();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            MinecraftServer server = event.getServer();
            BiomeInjector.addFeatures(((IMinecraftServerMixin)server).getRegistryHolder().registry(Registry.BIOME_REGISTRY).get());
        }
    }

    @SubscribeEvent
    public static void onPlayerNegotiation(PlayerNegotiationEvent playerNegotiationEvent) {
        EVPacketHandler.INSTANCE.sendTo(new S2CConfigAgreementPacket(
                ExcavatedVariants.oreStoneList.stream().flatMap(p -> p.last().stream().map(
                        stone -> new ResourceLocation(ExcavatedVariants.MOD_ID,
                                stone.id + "_" + p.first().id))).collect(Collectors.toSet())),
                playerNegotiationEvent.getConnection(),
                NetworkDirection.LOGIN_TO_CLIENT
        );
    }
}
