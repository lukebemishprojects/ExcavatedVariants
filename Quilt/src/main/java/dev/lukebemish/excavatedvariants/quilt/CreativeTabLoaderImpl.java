package dev.lukebemish.excavatedvariants.quilt;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.ICreativeTabLoader;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@SuppressWarnings("unused")
@AutoService(ICreativeTabLoader.class)
public class CreativeTabLoaderImpl implements ICreativeTabLoader {
    private static final int MAX_INT_VAL = (int) Math.sqrt(Integer.MAX_VALUE);
    public static CreativeModeTab EXCAVATED_VARIANTS_TAB;

    @Override
    public void registerCreativeTab() {
        EXCAVATED_VARIANTS_TAB = FabricItemGroup.builder(CREATIVE_TAB_ID)
                .icon(() -> new ItemStack(Items.DEEPSLATE_COPPER_ORE))
                .displayItems((featureFlagSet, output, bool) -> {
                    for (var supplier : ExcavatedVariants.getItems()) {
                        output.accept(supplier.get());
                    }
                })
                .build();
    }

    public CreativeModeTab getCreativeTab() {
        return EXCAVATED_VARIANTS_TAB;
    }
}
