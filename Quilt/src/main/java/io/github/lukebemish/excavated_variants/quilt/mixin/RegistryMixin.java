package io.github.lukebemish.excavated_variants.quilt.mixin;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public class RegistryMixin {

    @Inject(method = "register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private static <V, T extends V> void excavated_variants$registerListen(Registry<V> registry, ResourceKey<V> key, T item, CallbackInfoReturnable<T> cir) {
        ResourceLocation rl = key.location();
        if (registry == Registry.BLOCK) {
            ExcavatedVariants.loadedBlockRLs.add(rl);
            if (ExcavatedVariants.hasLoaded()) {
                for (ExcavatedVariants.RegistryFuture b : ExcavatedVariants.getBlockList()) {
                    if (ExcavatedVariants.loadedBlockRLs.contains(b.ore.block_id.get(0)) &&
                            ExcavatedVariants.loadedBlockRLs.contains(b.stone.block_id)) {
                        ExcavatedVariants.registerBlockAndItem(
                                (orl,bl)->Registry.register(Registry.BLOCK,orl,bl),
                                (orl,i)-> {
                                    Item out = Registry.register(Registry.ITEM, orl, i.get());
                                    return ()->out;
                                },b);
                    }
                }
            }
        }
    }
}
