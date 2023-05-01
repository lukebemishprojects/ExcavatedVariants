/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.platform.services.CreativeTabLoader;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("unused")
@AutoService(CreativeTabLoader.class)
public class CreativeTabLoaderImpl implements CreativeTabLoader {


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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreativeTabEvent);
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        return CreativeModeTabRegistry.getTab(CREATIVE_TAB_ID);
    }

    public void onCreativeTabEvent(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(CREATIVE_TAB_ID,
                CreativeTabLoaderImpl::setup);
    }
}
