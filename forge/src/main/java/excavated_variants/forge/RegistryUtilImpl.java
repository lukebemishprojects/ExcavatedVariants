package excavated_variants.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class RegistryUtilImpl {
    public static HashMap<ResourceLocation, Block> cache = new HashMap<>();

    public static Block getBlockById(ResourceLocation rl) {
        if (cache.containsKey(rl)) {
            return cache.get(rl);
        }
        if (ForgeRegistries.BLOCKS.containsKey(rl)) {
            Block out = ForgeRegistries.BLOCKS.getValue(rl);
            cache.put(rl,out);
            return out;
        }
        return null;
    }
}
