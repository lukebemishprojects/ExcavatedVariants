package dev.lukebemish.excavatedvariants.platform.services;

import dev.lukebemish.excavatedvariants.ExcavatedVariants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public interface CreativeTabLoader {
    ResourceLocation CREATIVE_TAB_ID = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
    void registerCreativeTab();
    CreativeModeTab getCreativeTab();
}
