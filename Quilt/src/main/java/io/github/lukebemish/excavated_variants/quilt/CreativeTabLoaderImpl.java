package io.github.lukebemish.excavated_variants.quilt;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.ICreativeTabLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;

@AutoService(ICreativeTabLoader.class)
public class CreativeTabLoaderImpl implements ICreativeTabLoader {
    private static final int MAX_INT_VAL = (int) Math.sqrt(Integer.MAX_VALUE);
    public static final CreativeModeTab EXCAVATED_VARIANTS_TAB = setup();

    private static CreativeModeTab setup() {
        var rl = new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants");
        return QuiltItemGroup.createWithIcon(rl, () -> {
                    if (ExcavatedVariants.getItems().isEmpty()) {
                        return new ItemStack(Items.DEEPSLATE_COPPER_ORE);
                    }
                    int time = (int) ((System.currentTimeMillis()/1000) % MAX_INT_VAL);
                    return new ItemStack(ExcavatedVariants.getItems().get((time*time) % ExcavatedVariants.getItems().size()).get());
                });
    }

    public CreativeModeTab getCreativeTab() {
        return EXCAVATED_VARIANTS_TAB;
    }
}
