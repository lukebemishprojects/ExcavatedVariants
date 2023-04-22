package dev.lukebemish.excavatedvariants.impl.forge;


import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.MissingVariantHelper;
import dev.lukebemish.excavatedvariants.impl.S2CConfigAgreementPacket;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreFinderUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerNegotiationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.stream.Collectors;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Ore Gen
        Services.REGISTRY_UTIL.reset();
        ExcavatedVariants.oreStoneList = null;
        OreFinderUtil.setupBlocks();
        ExcavatedVariants.setupMap();
    }

    @SubscribeEvent
    public static void onPlayerNegotiation(PlayerNegotiationEvent playerNegotiationEvent) {
        EVPacketHandler.INSTANCE.sendTo(new S2CConfigAgreementPacket(
                        ExcavatedVariants.oreStoneList.stream().flatMap(p -> p.getSecond().stream().map(
                                stone -> stone.id + "_" + p.getFirst().id)).collect(Collectors.toSet())),
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
            ResourceLocation newLocation = MissingVariantHelper.getBaseBlock(mapping.getKey().getPath());
            if (newLocation != null && mapping.getRegistry().containsKey(newLocation))
                mapping.remap(mapping.getRegistry().getValue(newLocation));
        }
    }
}
