package dev.lukebemish.excavatedvariants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public interface ICreativeTabLoader {
    static final ResourceLocation CREATIVE_TAB_ID = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
    void registerCreativeTab();
    CreativeModeTab getCreativeTab();
}
