package dev.lukebemish.excavatedvariants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface IRegistryUtil {
    void reset();

    @Nullable
    Block getBlockById(ResourceLocation rl);

    @Nullable
    Item getItemById(ResourceLocation rl);

    @Nullable
    ResourceLocation getRlByBlock(Block block);

    Iterable<Block> getAllBlocks();
}
