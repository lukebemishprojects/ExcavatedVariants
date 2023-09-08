/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.*;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreReplacer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    public static final DeferredRegister<Item> TO_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ExcavatedVariants.MOD_ID);
    public static final RegistryObject<Codec<AddFeatureLateModifier>> ADD_FEATURE_LATE_MODIFIER = BIOME_MODIFIERS.register("add_feature_late", () ->
            RecordCodecBuilder.create(i -> i.group(
                    PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeatureLateModifier::feature)
            ).apply(i, AddFeatureLateModifier::new))
    );

    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        ExcavatedVariants.init();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ExcavatedVariantsClient.init();
        }
        modbus.addListener(ExcavatedVariantsForge::commonSetup);
        modbus.addListener(ExcavatedVariantsForge::registerListener);
        TO_REGISTER.register(modbus);
        BIOME_MODIFIERS.register(modbus);
        FEATURES.register(modbus);
        CREATIVE_TABS.register(modbus);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        //ModList.get().getModContainerById("hyle").ifPresent(container -> MinecraftForge.EVENT_BUS.register(new HyleCompat()));

        EVPacketHandler.INSTANCE.registerMessage(0, S2CConfigAgreementPacket.class, S2CConfigAgreementPacket::encoder, S2CConfigAgreementPacket::decoder, (msg, c) -> {
            c.get().enqueueWork(() -> msg.consumeMessage(string -> c.get().getNetworkManager().disconnect(Component.literal(string))));
            c.get().setPacketHandled(true);
        });

        FEATURES.register("ore_replacer", OreReplacer::new);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(RegistriesImpl::registerRegistries);
    }

    public static void registerListener(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            BuiltInRegistries.BLOCK.holders().forEach(reference ->
                    BlockAddedCallback.onRegister(reference.value(), reference.key()));
            BlockAddedCallback.setReady();
            BlockAddedCallback.register();
        });
        event.register(ForgeRegistries.Keys.ITEMS, helper ->
                ExcavatedVariants.initPostRegister());
    }
}
