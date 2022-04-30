package com.github.lukebemish.excavated_variants.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryUtilImpl {
    public static Map<ResourceLocation, Block> block_cache = new ConcurrentHashMap<>();
    public static Map<Block, ResourceLocation> block_rl_cache = new ConcurrentHashMap<>();
    public static Map<ResourceLocation, Item> item_cache = new ConcurrentHashMap<>();

    public static void reset() {
        block_cache.clear();
        block_rl_cache.clear();
        item_cache.clear();
    }

    public static Block getBlockById(ResourceLocation rl) {
        if (block_cache.containsKey(rl)) {
            return block_cache.get(rl);
        }
        if (Registry.BLOCK.containsKey(rl)) {
            Block out = Registry.BLOCK.get(rl);
            block_cache.put(rl,out);
            return out;
        }
        return null;
    }

    public static Item getItemById(ResourceLocation rl) {
        if (item_cache.containsKey(rl)) {
            return item_cache.get(rl);
        }
        if (Registry.ITEM.containsKey(rl)) {
            Item out = Registry.ITEM.get(rl);
            item_cache.put(rl,out);
            return out;
        }
        return null;
    }

    public static ResourceLocation getRlByBlock(Block block) {
        if (block_rl_cache.containsKey(block)) {
            return block_rl_cache.get(block);
        }
        ResourceLocation rl = Registry.BLOCK.getKey(block);
        block_rl_cache.put(block,rl);
        return rl;
    }

    public static Iterable<Block> getAllBlocks() {
        return Registry.BLOCK;
    }
}
