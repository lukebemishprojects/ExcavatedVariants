/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.recipe;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreConversionRecipe extends CustomRecipe {
    public static final Map<ResourceLocation, List<ResourceKey<Block>>> ORE_MAP = new HashMap<>();
    public static Map<Item, Item> itemMap;

    public OreConversionRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
    }

    public static synchronized void assembleMap() {
        if (itemMap != null) {
            return;
        }
        itemMap = new HashMap<>();
        for (ResourceLocation rl : ORE_MAP.keySet()) {
            Item itemIn = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
            if (itemIn == null) continue;
            Block blockOut = null;
            for (ResourceKey<Block> blockKey : ORE_MAP.get(rl)) {
                blockOut = BuiltInRegistries.BLOCK.get(blockKey);
                if (blockOut != null) break;
            }
            if (blockOut == null) continue;
            Item itemOut = blockOut.asItem();
            itemMap.put(itemIn, itemOut);
        }
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (!ExcavatedVariants.getConfig().addConversionRecipes) {
            return false;
        }
        assembleMap();
        ItemStack singleStack = null;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (singleStack != null) return false;
            singleStack = itemStack;
        }
        if (singleStack != null) {
            Item item = itemMap.get(singleStack.getItem());
            return item != null;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        assembleMap();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            if (itemStack.isEmpty()) continue;
            Item item = itemMap.get(itemStack.getItem());
            return item == null ? ItemStack.EMPTY : new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ExcavatedVariants.ORE_CONVERSION.get();
    }
}
