package com.github.lukebemish.excavated_variants.forge;


import com.github.lukebemish.excavated_variants.BiomeInjector;
import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.mixin.IMinecraftServerMixin;
import com.github.lukebemish.excavated_variants.worldgen.OreFinderUtil;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Ore Gen
        RegistryUtil.reset();
        ExcavatedVariants.oreStoneList = null;
        OreFinderUtil.reset();
        ExcavatedVariants.setupMap();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            MinecraftServer server = event.getServer();
            BiomeInjector.addFeatures(((IMinecraftServerMixin)server).getRegistryHolder().registry(Registry.BIOME_REGISTRY).get());
        }
    }
}
