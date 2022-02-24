package excavated_variants.forge;

import dev.architectury.registry.block.BlockProperties;
import excavated_variants.data.BaseOre;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class ExcavatedVariantsImpl {
    public static void registerFeatures() {
    }
    public static Block makeDefaultOreBlock(String id, BaseOre ore) {
        Block block = new ForgeOreBlock(BlockProperties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f), ore);
        return block;
    }
}
