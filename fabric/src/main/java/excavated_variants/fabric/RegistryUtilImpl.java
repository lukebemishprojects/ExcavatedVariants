package excavated_variants.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class RegistryUtilImpl {
    public static Block getBlockById(ResourceLocation rl) {
        if (Registry.BLOCK.containsKey(rl)) {
            return Registry.BLOCK.get(rl);
        }
        return null;
    }
}
