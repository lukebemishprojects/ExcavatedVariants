package excavated_variants.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryUtilImpl {
    public static Block getBlockById(ResourceLocation rl) {
        if (ForgeRegistries.BLOCKS.containsKey(rl)) {
            return ForgeRegistries.BLOCKS.getValue(rl);
        }
        return null;
    }
}
