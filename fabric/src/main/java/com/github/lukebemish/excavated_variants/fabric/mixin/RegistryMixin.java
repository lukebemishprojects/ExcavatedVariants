package com.github.lukebemish.excavated_variants.fabric.mixin;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import com.google.common.base.Stopwatch;
import lilypuree.unearthed.CommonMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public class RegistryMixin {

    @Inject(method = "register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("HEAD"))
    private static <V, T extends V> void onRegister(Registry<V> registry, ResourceLocation rl, T item, CallbackInfoReturnable<T> cir) {
        if (registry == Registry.BLOCK) {
            ExcavatedVariants.loadedBlockRLs.add(rl);
            if (ExcavatedVariants.hasLoaded()) {
                for (ModifiedOreBlock b : ExcavatedVariants.getBlocks().values()) {
                    if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.rl_block_id.get(0)) &&
                            ExcavatedVariants.loadedBlockRLs.contains(b.stone.rl_block_id)) {
                        b.copyBlockstateDefs();
                    }
                }
            }
        }
    }
}
