/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.platform.services.CreativeTabLoader;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.DeferredRegister;

@SuppressWarnings("unused")
@AutoService(CreativeTabLoader.class)
public class CreativeTabLoaderImpl implements CreativeTabLoader {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExcavatedVariants.MOD_ID);

    private static void setup(CreativeModeTab.Builder builder) {
        builder
                .title(Component.translatable("itemGroup."+CREATIVE_TAB_ID.getNamespace()+"."+CREATIVE_TAB_ID.getPath()))
                .icon(() -> new ItemStack(Items.DEEPSLATE_COPPER_ORE))
                .displayItems((displayParameters, output) -> {
                    for (var supplier : ExcavatedVariants.getItems()) {
                        output.accept(supplier.get());
                    }
                });
    }

    @Override
    public void registerCreativeTab() {
        CREATIVE_TABS.register(CREATIVE_TAB_ID.getPath(), () -> {
            var builder = CreativeModeTab.builder();
            setup(builder);
            return builder.build();
        });
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        return CreativeModeTabRegistry.getTab(CREATIVE_TAB_ID);
    }
}
