package io.github.lukebemish.excavated_variants.quilt;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.quilt.compat.HyleCompat;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.worldgen.OreFinderUtil;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public class ExcavatedVariantsQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer modContainer) {
        ExcavatedVariants.init();
        for (ExcavatedVariants.RegistryFuture b : ExcavatedVariants.getBlockList()) {
            if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.block_id.get(0)) &&
                    ExcavatedVariants.loadedBlockRLs.contains(b.stone.block_id)) {
                ExcavatedVariants.registerBlockAndItem(
                        (rl,bl)->Registry.register(Registry.BLOCK,rl,bl),
                        (rl,i)->{
                            Item out = Registry.register(Registry.ITEM,rl,i.get());
                            return ()->out;
                        },b);
            }
        }
        ServerLifecycleEvents.STARTING.register(server -> {
            //Ore gen map setup
            Services.REGISTRY_UTIL.reset();
            ExcavatedVariants.oreStoneList = null;
            OreFinderUtil.setupBlocks();
            ExcavatedVariants.setupMap();
        });
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            ResourceKey<PlacedFeature> confKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"));
            // A bit hacky, but will hopefully put it after existing stuff (like Unearthed's generation)
            BiomeModifications.create(confKey.location()).add(ModificationPhase.POST_PROCESSING, (x) -> true, context -> {
                context.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, confKey);
            });
        }

        if (FabricLoader.getInstance().isModLoaded("unearthed") && ExcavatedVariants.setupMap()) {
            HyleCompat.init();
        }
    }
}
