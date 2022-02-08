package excavated_variants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class RegistryUtil {
    @ExpectPlatform
    public static Block getBlockById(ResourceLocation rl) {
        throw new AssertionError();
    }
}
