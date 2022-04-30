package com.github.lukebemish.excavated_variants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class RegistryUtil {
    @ExpectPlatform
    public static void reset() {
        throw new AssertionError();
    }
    @ExpectPlatform
    @Nullable
    public static Block getBlockById(ResourceLocation rl) {
        throw new AssertionError();
    }
    @ExpectPlatform
    @Nullable
    public static Item getItemById(ResourceLocation rl) {
        throw new AssertionError();
    }
    @ExpectPlatform
    @Nullable
    public static ResourceLocation getRlByBlock(Block block) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Iterable<Block> getAllBlocks() {
        throw new AssertionError();
    }
}
