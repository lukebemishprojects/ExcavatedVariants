/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.BlockAddedCallback;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariantsClient;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreReplacer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsNeoForge {
    public static final DeferredRegister<Item> TO_REGISTER = DeferredRegister.create(Registries.ITEM, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExcavatedVariants.MOD_ID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ExcavatedVariants.MOD_ID);
    public static final Holder<Codec<? extends BiomeModifier>> ADD_FEATURE_LATE_MODIFIER = BIOME_MODIFIERS.register("add_feature_late", () ->
            RecordCodecBuilder.<AddFeatureLateModifier>create(i -> i.group(
                    PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeatureLateModifier::feature)
            ).apply(i, AddFeatureLateModifier::new))
    );

    public ExcavatedVariantsNeoForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        ExcavatedVariants.init();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ExcavatedVariantsClient.init();
        }
        modbus.addListener(ExcavatedVariantsNeoForge::commonSetup);
        modbus.addListener(ExcavatedVariantsNeoForge::registerListener);
        TO_REGISTER.register(modbus);
        BIOME_MODIFIERS.register(modbus);
        FEATURES.register(modbus);
        CREATIVE_TABS.register(modbus);
        NeoForge.EVENT_BUS.register(EventHandler.class);
        //ModList.get().getModContainerById("hyle").ifPresent(container -> MinecraftForge.EVENT_BUS.register(new HyleCompat()));

        // TODO: Reimplement network checks, hopefully with new API
        /*
        EVPacketHandler.INSTANCE.registerMessage(0, S2CConfigAgreementPacket.class, S2CConfigAgreementPacket::encoder, S2CConfigAgreementPacket::decoder, (msg, c) -> {
            c.enqueueWork(() -> msg.consumeMessage(string -> c.getNetworkManager().disconnect(Component.literal(string))));
            c.setPacketHandled(true);
        });
        */

        FEATURES.register("ore_replacer", OreReplacer::new);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(RegistriesImpl::registerRegistries);
    }

    public static void registerListener(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            BuiltInRegistries.BLOCK.holders().forEach(reference ->
                    BlockAddedCallback.onRegister(reference.value(), reference.key()));
            BlockAddedCallback.setReady();
            BlockAddedCallback.register();
        });
        event.register(Registries.ITEM, helper ->
                ExcavatedVariants.initPostRegister());
    }
}
