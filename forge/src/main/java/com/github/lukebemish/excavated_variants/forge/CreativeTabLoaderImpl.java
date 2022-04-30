package com.github.lukebemish.excavated_variants.forge;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class CreativeTabLoaderImpl {
    private static final int MAX_INT_VAL = (int) Math.sqrt(Integer.MAX_VALUE);
    public static final CreativeModeTab EXCAVATED_VARIANTS_TAB = setup();

    private static CreativeModeTab setup() {
        var rl = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
        return new CreativeModeTab(String.format("%s.%s", rl.getNamespace(), rl.getPath())) {
            @Override
            public @NotNull ItemStack makeIcon() {
                return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public @NotNull ItemStack getIconItem() {
                if (ExcavatedVariants.getItems().isEmpty()) {
                    return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
                }
                int time = (int) ((System.currentTimeMillis()/1000) % MAX_INT_VAL);
                return new ItemStack(ExcavatedVariants.getItems().get((time*time) % ExcavatedVariants.getItems().size()).get());
            }
        };
    }

    public static CreativeModeTab getCreativeTab() {
        return EXCAVATED_VARIANTS_TAB;
    }
}
