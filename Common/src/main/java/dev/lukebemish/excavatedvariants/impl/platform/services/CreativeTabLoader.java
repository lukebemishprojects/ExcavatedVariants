package dev.lukebemish.excavatedvariants.impl.platform.services;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public interface CreativeTabLoader {
    ResourceLocation CREATIVE_TAB_ID = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
    void registerCreativeTab();
    CreativeModeTab getCreativeTab();
}
