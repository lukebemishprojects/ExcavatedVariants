package excavated_variants.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;

public class RegistryUtilImpl {
    public static HashMap<ResourceLocation, Block> cache = new HashMap<>();

    public static Block getBlockById(ResourceLocation rl) {
        if (cache.containsKey(rl)) {
            return cache.get(rl);
        }
        if (Registry.BLOCK.containsKey(rl)) {
            Block out = Registry.BLOCK.get(rl);
            cache.put(rl,out);
            return out;
        }
        return null;
    }
}
