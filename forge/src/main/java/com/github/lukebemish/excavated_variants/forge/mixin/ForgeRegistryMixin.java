package com.github.lukebemish.excavated_variants.forge.mixin;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.google.common.collect.BiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ForgeRegistry.class,remap=false)
public abstract class ForgeRegistryMixin<V extends IForgeRegistryEntry<V>> {

    @Shadow
    @Final
    private Class<V> superType;

    @Shadow
    @Final
    private BiMap<ResourceLocation, V> names;

    @Inject(method = "freeze", at = @At("HEAD"))
    private void excavated_variants$registryFreezeHackery(CallbackInfo ci) {
        if (superType == Block.class) {
            this.names.forEach((rl, value) -> {
                ExcavatedVariants.loadedBlockRLs.add(rl);
            });
            if (ExcavatedVariants.hasLoaded()) {
                for (ExcavatedVariants.RegistryFuture b : ExcavatedVariants.getBlockList()) {
                    if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.block_id.get(0)) &&
                            ExcavatedVariants.loadedBlockRLs.contains(b.stone.block_id)) {
                        ExcavatedVariants.registerBlockAndItem((rlr,bl)->{
                            bl.setRegistryName(rlr);
                            ForgeRegistries.BLOCKS.register(bl);
                        },(rlr,it)->{
                            it.setRegistryName(rlr);
                            ForgeRegistries.ITEMS.register(it);
                        },b);
                    }
                }
            }
        }
    }
}
