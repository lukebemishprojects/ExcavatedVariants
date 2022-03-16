package com.github.lukebemish.excavated_variants.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

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
        if (ForgeRegistries.BLOCKS.containsKey(rl)) {
            Block out = ForgeRegistries.BLOCKS.getValue(rl);
            block_cache.put(rl,out);
            return out;
        }
        return null;
    }

    public static Item getItemById(ResourceLocation rl) {
        if (item_cache.containsKey(rl)) {
            return item_cache.get(rl);
        }
        if (ForgeRegistries.ITEMS.containsKey(rl)) {
            Item out = ForgeRegistries.ITEMS.getValue(rl);
            item_cache.put(rl,out);
            return out;
        }
        return null;
    }

    public static ResourceLocation getRlByBlock(Block block) {
        if (block_rl_cache.containsKey(block)) {
            return block_rl_cache.get(block);
        }
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
        block_rl_cache.put(block,rl);
        return rl;
    }
}
