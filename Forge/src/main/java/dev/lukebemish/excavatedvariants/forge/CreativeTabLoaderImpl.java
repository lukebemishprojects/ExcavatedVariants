package dev.lukebemish.excavatedvariants.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.ICreativeTabLoader;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("unused")
@AutoService(ICreativeTabLoader.class)
public class CreativeTabLoaderImpl implements ICreativeTabLoader {


    private static void setup(CreativeModeTab.Builder builder) {
        builder
                .icon(() -> new ItemStack(Items.DEEPSLATE_COPPER_ORE))
                .displayItems((featureFlagSet, output, bool) -> {
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
