package excavated_variants.forge;

import dev.architectury.platform.forge.EventBuses;
import excavated_variants.ExcavatedVariants;
import excavated_variants.ExcavatedVariantsClient;
import excavated_variants.forge.compat.UECompat;
import excavated_variants.worldgen.OreReplacer;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ExcavatedVariants.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_REPLACER = FEATURES.register("ore_replacer", OreReplacer::new);

    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(ExcavatedVariants.MOD_ID, modbus);
        ExcavatedVariants.init();
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ExcavatedVariantsClient::init);
        modbus.addListener(ExcavatedVariantsForge::commonSetup);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        ModList.get().getModContainerById("unearthed").ifPresent(container -> {
            int major = container.getModInfo().getVersion().getMajorVersion();
            int minor = container.getModInfo().getVersion().getMinorVersion();
            if (major >= 2 && minor >= 2 && ExcavatedVariants.isMapSetupCorrectly())
                MinecraftForge.EVENT_BUS.register(new UECompat());
        });
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            final ConfiguredFeature<NoneFeatureConfiguration, ?> ORE_REPLACER_CONFIGURED = ExcavatedVariantsForge.ORE_REPLACER.get().configured(NoneFeatureConfiguration.NONE);
            final PlacedFeature ORE_REPLACER_PLACED = ORE_REPLACER_CONFIGURED.placed();
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ORE_REPLACER_CONFIGURED);
            Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), ORE_REPLACER_PLACED);
        });
    }
}
