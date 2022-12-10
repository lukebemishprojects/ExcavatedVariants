package dev.lukebemish.excavatedvariants.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.IRegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AutoService(IRegistryUtil.class)
public class RegistryUtilImpl implements IRegistryUtil {
    public static Map<ResourceLocation, Block> blockCache = new ConcurrentHashMap<>();
    public static Map<Block, ResourceLocation> blockRlCache = new ConcurrentHashMap<>();
    public static Map<ResourceLocation, Item> itemCache = new ConcurrentHashMap<>();

    public void reset() {
        blockCache.clear();
        blockRlCache.clear();
        itemCache.clear();
    }

    public Block getBlockById(ResourceLocation rl) {
        if (blockCache.containsKey(rl)) {
            return blockCache.get(rl);
        }
        if (ForgeRegistries.BLOCKS.containsKey(rl)) {
            Block out = ForgeRegistries.BLOCKS.getValue(rl);
            blockCache.put(rl, out);
            return out;
        }
        return null;
    }

    public Item getItemById(ResourceLocation rl) {
        if (itemCache.containsKey(rl)) {
            return itemCache.get(rl);
        }
        if (ForgeRegistries.ITEMS.containsKey(rl)) {
            Item out = ForgeRegistries.ITEMS.getValue(rl);
            itemCache.put(rl, out);
            return out;
        }
        return null;
    }

    public ResourceLocation getRlByBlock(Block block) {
        if (blockRlCache.containsKey(block)) {
            return blockRlCache.get(block);
        }
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
        blockRlCache.put(block, rl);
        return rl;
    }

    public Iterable<Block> getAllBlocks() {
        return ForgeRegistries.BLOCKS.getValues();
    }
}
