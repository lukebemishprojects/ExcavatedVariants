package excavated_variants;

import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreativeTabLoader {
    public static final CreativeModeTab EXCAVATED_VARIANTS_TAB = CreativeTabRegistry.create(new ResourceLocation(ExcavatedVariants.MOD_ID, "excavated_variants"), () ->
            new ItemStack(Items.COPPER_ORE));
}
