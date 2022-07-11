package io.github.lukebemish.excavated_variants.forge;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.ExcavatedVariantsClient;
import io.github.lukebemish.excavated_variants.S2CConfigAgreementPacket;
import io.github.lukebemish.excavated_variants.forge.compat.HyleCompat;
import io.github.lukebemish.excavated_variants.forge.registry.BlockAddedCallback;
import io.github.lukebemish.excavated_variants.worldgen.OreReplacer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ExcavatedVariants.MOD_ID);

    public static final DeferredRegister<Item> toRegister = DeferredRegister.create(ForgeRegistries.ITEMS, ExcavatedVariants.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_REPLACER = FEATURES.register("ore_replacer", OreReplacer::new);

    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        ExcavatedVariants.init();
        FEATURES.register(modbus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ExcavatedVariantsClient::init);
        modbus.addListener(ExcavatedVariantsForge::commonSetup);
        modbus.addListener(ExcavatedVariantsForge::registerListener);
        toRegister.register(modbus);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        ModList.get().getModContainerById("hyle").ifPresent(container -> MinecraftForge.EVENT_BUS.register(new HyleCompat()));
        MainPlatformTargetImpl.RECIPE_SERIALIZERS.register(modbus);

        EVPacketHandler.INSTANCE.registerMessage(0, S2CConfigAgreementPacket.class, S2CConfigAgreementPacket::encoder, S2CConfigAgreementPacket::decoder, (msg, c) -> {
            c.get().enqueueWork(() -> msg.consumeMessage(string -> c.get().getNetworkManager().disconnect(Component.literal(string))));
            c.get().setPacketHandled(true);
        });
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ExcavatedVariants.ORE_REPLACER_CONFIGURED = new ConfiguredFeature<>(ORE_REPLACER.get(), FeatureConfiguration.NONE);
            ExcavatedVariants.ORE_REPLACER_PLACED = new PlacedFeature(Holder.direct(ExcavatedVariants.ORE_REPLACER_CONFIGURED), List.of());
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ExcavatedVariants.ORE_REPLACER_CONFIGURED);
            Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ExcavatedVariants.ORE_REPLACER_PLACED);

            ExcavatedVariants.getMappingsCache();
        });
    }

    public static void registerListener(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            ExcavatedVariants.loadedBlockRLs.addAll(ForgeRegistries.BLOCKS.getKeys().stream().filter(ExcavatedVariants.neededRls::contains).toList());
            BlockAddedCallback.register();
        });
    }

}
