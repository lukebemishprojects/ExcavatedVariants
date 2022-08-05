package io.github.lukebemish.excavated_variants.recipe;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class OreConversionRecipe extends CustomRecipe {
    public static final Map<ResourceLocation, ResourceLocation> oreMap = new HashMap<>();
    public static Map<Item, Pair<ResourceLocation, Item>> itemMap;

    public OreConversionRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public static void assembleOrNull() {
        if (itemMap != null) {
            return;
        }
        itemMap = new HashMap<>();
        for (ResourceLocation rl : oreMap.keySet()) {
            Item itemIn = Services.REGISTRY_UTIL.getItemById(rl);
            Item itemOut = Services.REGISTRY_UTIL.getItemById(oreMap.get(rl));
            if (itemIn == null || itemOut == null) continue;
            itemMap.put(itemIn, new Pair<>(rl, itemOut));
        }
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        assembleOrNull();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemMap.keySet().stream().anyMatch(itemStack::is)) {
                Item item = itemMap.keySet().stream().filter(itemStack::is).toList().get(0);
                return item != null;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        assembleOrNull();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemMap.keySet().stream().anyMatch(itemStack::is)) {
                Item item = itemMap.keySet().stream().filter(itemStack::is).toList().get(0);
                return item == null ? null : new ItemStack(itemMap.get(item).getSecond());
            }
        }
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ExcavatedVariants.ORE_CONVERSION.get();
    }
}
