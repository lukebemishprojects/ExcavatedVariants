package io.github.lukebemish.excavated_variants.forge.mixin;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(value = GameData.class, remap = false,priority = 1998)
public class GameDataMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger("Excavated Variants Mixin Hackery");
    @Unique
    private static boolean hasSuppressed;

    @Inject(method="checkPrefix",at=@At("HEAD"),cancellable = true)
    private static void excavated_variants$logKillingHackery(String name, boolean warnOverrides, CallbackInfoReturnable<ResourceLocation> cir) {
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index).toLowerCase(Locale.ROOT);
        String name2 = index == -1 ? name : name.substring(index + 1);
        if (oldPrefix.equals(ExcavatedVariants.MOD_ID) && warnOverrides) {
            if (!hasSuppressed) {
                hasSuppressed = true;
                LOGGER.warn("The following message about a potentially Dangerous alternative prefix is the first of " +
                        "many, and they all look the same. The rest will be suppressed.");
            } else {
                cir.setReturnValue(new ResourceLocation(oldPrefix, name2));
            }
        }
    }
}
