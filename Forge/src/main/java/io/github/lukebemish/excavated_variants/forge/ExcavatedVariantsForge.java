package io.github.lukebemish.excavated_variants.forge;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.ExcavatedVariantsClient;
import io.github.lukebemish.excavated_variants.forge.compat.HyleCompat;
import io.github.lukebemish.excavated_variants.worldgen.OreReplacer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ExcavatedVariants.MOD_ID);

    public static final ArrayList<Supplier<Item>> toRegister = new ArrayList<>();

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_REPLACER = FEATURES.register("ore_replacer", OreReplacer::new);

    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        ExcavatedVariants.init();
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ExcavatedVariantsClient.init();
            modbus.addListener(ExcavatedVariantsForgeClient::clientSetup);
        });
        modbus.addListener(ExcavatedVariantsForge::commonSetup);
        modbus.addGenericListener(Item.class, ExcavatedVariantsForge::registerItems);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        ModList.get().getModContainerById("hyle").ifPresent(container -> {
            if (major >= 2 && minor >= 2 && ExcavatedVariants.setupMap())
                MinecraftForge.EVENT_BUS.register(new HyleCompat());
        });
        MainPlatformTargetImpl.RECIPE_SERIALIZERS.register(modbus);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ExcavatedVariants.ORE_REPLACER_CONFIGURED = new ConfiguredFeature<>(ORE_REPLACER.get(), FeatureConfiguration.NONE);
            ExcavatedVariants.ORE_REPLACER_PLACED = new PlacedFeature(Holder.direct(ExcavatedVariants.ORE_REPLACER_CONFIGURED), List.of());
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ExcavatedVariants.ORE_REPLACER_CONFIGURED);
            Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ExcavatedVariants.ORE_REPLACER_PLACED);
        });
    }

    public static void registerItems(RegistryEvent.Register<Item> e) {
        for (Supplier<Item> si : toRegister) {
            e.getRegistry().register(si.get());
        }
    }

}
