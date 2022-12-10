package dev.lukebemish.excavatedvariants.mixin;

import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.platform.Services;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @Unique
    private static final int MAX_INT_VAL = (int) Math.sqrt(Integer.MAX_VALUE);

    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void excavated_variants$itemIconInjection(CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this != Services.CREATIVE_TAB_LOADER.get().getCreativeTab()) {
            return;
        }
        if (ExcavatedVariants.getItems().isEmpty()) {
            return;
        }
        int time = (int) ((System.currentTimeMillis() / 1000) % MAX_INT_VAL);
        cir.setReturnValue(new ItemStack(ExcavatedVariants.getItems().get((time * time) % ExcavatedVariants.getItems().size()).get()));
    }
}
