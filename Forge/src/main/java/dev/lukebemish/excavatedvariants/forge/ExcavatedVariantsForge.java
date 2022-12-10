package dev.lukebemish.excavatedvariants.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.ExcavatedVariantsClient;
import dev.lukebemish.excavatedvariants.S2CConfigAgreementPacket;
import dev.lukebemish.excavatedvariants.forge.compat.HyleCompat;
import dev.lukebemish.excavatedvariants.forge.registry.BlockAddedCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
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

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    public static final DeferredRegister<Item> TO_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ExcavatedVariants.MOD_ID);

    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ExcavatedVariants.MOD_ID);
    public static final RegistryObject<Codec<AddFeatureLateModifier>> ADD_FEATURE_LATE_MODIFIER = BIOME_MODIFIERS.register("add_feature_late", () ->
            RecordCodecBuilder.create(i -> i.group(
                    PlacedFeature.CODEC.fieldOf("feature").forGetter(AddFeatureLateModifier::feature)
            ).apply(i, AddFeatureLateModifier::new))
    );

    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        ExcavatedVariants.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ExcavatedVariantsClient::init);
        modbus.addListener(ExcavatedVariantsForge::commonSetup);
        modbus.addListener(ExcavatedVariantsForge::registerListener);
        TO_REGISTER.register(modbus);
        BIOME_MODIFIERS.register(modbus);
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
