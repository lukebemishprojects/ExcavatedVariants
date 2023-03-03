package dev.lukebemish.excavatedvariants.forge.registry;

import com.google.common.base.Suppliers;
import dev.lukebemish.excavatedvariants.platform.Services;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.forge.ExcavatedVariantsForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.function.Supplier;

public class BlockAddedCallback {
    private static final Supplier<ModContainer> EV_CONTAINER = Suppliers.memoize(() -> ModList.get().getModContainerById(ExcavatedVariants.MOD_ID).orElseThrow());

    private static boolean isRegistering = false;

    public static void register() {
        if (ExcavatedVariants.hasLoaded() && !isRegistering) {
            isRegistering = true;
            ArrayList<ExcavatedVariants.RegistryFuture> toRemove = new ArrayList<>();
            for (ExcavatedVariants.RegistryFuture b : ExcavatedVariants.getBlockList()) {
                if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.blockId.get(0)) &&
                        ExcavatedVariants.loadedBlockRLs.contains(b.stone.blockId)) {
                    ExcavatedVariants.registerBlockAndItem((rlr, bl) -> {
                        final ModContainer activeContainer = ModLoadingContext.get().getActiveContainer();
                        ModLoadingContext.get().setActiveContainer(EV_CONTAINER.get());
                        ForgeRegistries.BLOCKS.register(rlr, bl);
                        ModLoadingContext.get().setActiveContainer(activeContainer);
                    }, (rlr, it) -> {
                        ExcavatedVariantsForge.TO_REGISTER.register(rlr.getPath(), it);
                        return () -> Services.REGISTRY_UTIL.getItemById(rlr);
                    }, b);
                    toRemove.add(b);
                }
            }
            ExcavatedVariants.blockList.removeAll(toRemove);
            isRegistering = false;
        }
    }
}
