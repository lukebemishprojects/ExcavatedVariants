package com.github.lukebemish.excavated_variants.forge;


import com.github.lukebemish.excavated_variants.BiomeInjector;
import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.github.lukebemish.excavated_variants.RegistryUtil;
import com.github.lukebemish.excavated_variants.mixin.MinecraftServerMixin;
import com.github.lukebemish.excavated_variants.worldgen.OreFinderUtil;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {
/*
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void biomeModifier(BiomeLoadingEvent event) {
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal()+1, Holder.Reference.createStandAlone(BuiltinRegistries.PLACED_FEATURE,ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY,new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"))));
        }
    }
*/
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Properties
        for (ModifiedOreBlock block : ExcavatedVariants.getBlocks().values()) {
            block.copyProperties();
        }
        //Ore Gen
        RegistryUtil.reset();
        ExcavatedVariants.oreStoneList = null;
        OreFinderUtil.reset();
        ExcavatedVariants.setupMap();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            MinecraftServer server = event.getServer();
            BiomeInjector.addFeatures(((MinecraftServerMixin)server).getRegistryHolder().registry(Registry.BIOME_REGISTRY).get());
        }
    }
}
