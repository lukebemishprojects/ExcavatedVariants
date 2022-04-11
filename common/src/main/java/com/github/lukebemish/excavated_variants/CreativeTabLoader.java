package com.github.lukebemish.excavated_variants;

import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreativeTabLoader {
    public static final CreativeModeTab EXCAVATED_VARIANTS_TAB = CreativeTabRegistry.create(new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants"), () ->
    {
        if (ExcavatedVariants.getItems().isEmpty()) {
            return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
        }
        return new ItemStack(ExcavatedVariants.getItems().get(512 % ExcavatedVariants.getItems().size()).get());
    });
}
