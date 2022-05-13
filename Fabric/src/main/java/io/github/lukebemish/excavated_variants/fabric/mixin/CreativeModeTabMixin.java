package io.github.lukebemish.excavated_variants.fabric.mixin;

import io.github.lukebemish.excavated_variants.fabric.ICreativeTabExtender;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin implements ICreativeTabExtender {
    @Shadow
    @Final
    @Mutable
    public static CreativeModeTab[] TABS;

    @Override
    public void excavated_variants$extend_array() {
        CreativeModeTab[] tempGroups = TABS;
        TABS = new CreativeModeTab[TABS.length + 1];

        System.arraycopy(tempGroups, 0, TABS, 0, tempGroups.length);
    }
}
