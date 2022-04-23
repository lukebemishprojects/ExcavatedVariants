package com.github.lukebemish.excavated_variants.fabric;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreativeTabLoaderImpl {
    public static final CreativeModeTab EXCAVATED_VARIANTS_TAB = setup();

    private static CreativeModeTab setup() {
        ((ItemGroupExtensions) CreativeModeTab.TAB_BUILDING_BLOCKS).fabric_expandArray();
        var rl = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
        return new CreativeModeTab(CreativeModeTab.TABS.length - 1, String.format("%s.%s", rl.getNamespace(), rl.getPath())) {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
            }

            @Override
            @Environment(EnvType.CLIENT)
            public ItemStack getIconItem() {
                if (ExcavatedVariants.getItems().isEmpty()) {
                    return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
                }
                return new ItemStack(ExcavatedVariants.getItems().get((((int)System.currentTimeMillis())/1000)*((int)System.currentTimeMillis())/1000 % ExcavatedVariants.getItems().size()).get());
            }
        };
    }

    public static CreativeModeTab getCreativeTab() {
        return EXCAVATED_VARIANTS_TAB;
    }
}
