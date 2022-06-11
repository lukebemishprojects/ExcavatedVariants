package io.github.lukebemish.excavated_variants.forge.mixin;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.forge.ExcavatedVariantsForge;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value=ForgeRegistry.class,remap=false)
public abstract class ForgeRegistryMixin<V> {

    @Shadow
    @Final
    private ResourceKey<Registry<V>> key;

    @Shadow
    @Final
    private BiMap<ResourceLocation, V> names;

    @Unique
    private static final Supplier<ModContainer> EV_CONTAINER = Suppliers.memoize(() -> ModList.get().getModContainerById(ExcavatedVariants.MOD_ID).orElseThrow());

    @Inject(method = "freeze", at = @At("HEAD"))
    private void excavated_variants$registryFreezeHackery(CallbackInfo ci) {
        if (key.equals(ForgeRegistries.Keys.BLOCKS)) {
            this.names.forEach((rl, value) -> {
                ExcavatedVariants.loadedBlockRLs.add(rl);
            });
            if (ExcavatedVariants.hasLoaded()) {
                for (ExcavatedVariants.RegistryFuture b : ExcavatedVariants.getBlockList()) {
                    if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.block_id.get(0)) &&
                            ExcavatedVariants.loadedBlockRLs.contains(b.stone.block_id)) {
                        ExcavatedVariants.registerBlockAndItem((rlr,bl)->{
                            final ModContainer activeContainer = ModLoadingContext.get().getActiveContainer();
                            ModLoadingContext.get().setActiveContainer(EV_CONTAINER.get());
                            ForgeRegistries.BLOCKS.register(rlr, bl);
                            ModLoadingContext.get().setActiveContainer(activeContainer);
                        },(rlr,it)->{
                            ExcavatedVariantsForge.toRegister.register(rlr.getPath(), it);
                            return ()-> Services.REGISTRY_UTIL.getItemById(rlr);
                        },b);
                    }
                }
            }
        }
    }
}
