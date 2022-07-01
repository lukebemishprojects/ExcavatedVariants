package io.github.lukebemish.excavated_variants.compat.rei;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
import io.github.lukebemish.excavated_variants.util.Pair;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.*;

public class ExcavatedVariantsClientPlugin implements REIClientPlugin {
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        if (ExcavatedVariants.getConfig().add_conversion_recipes && ExcavatedVariants.getConfig().jei_rei_compat) {
            List<CraftingRecipe> recipes = new ArrayList<>();
            OreConversionRecipe.assembleOrNull();
            for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                ArrayList<Item> items = new ArrayList<>();
                for (BaseStone stone : p.last()) {
                    ResourceLocation rl = new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + p.first().id);
                    Item item = Services.REGISTRY_UTIL.getItemById(rl);
                    if (item != null) {
                        items.add(item);
                    }
                }
                Item outItem = Services.REGISTRY_UTIL.getItemById(p.first().block_id.get(0));
                if (!items.isEmpty() && outItem != null) {
                    Ingredient input = Ingredient.of(items.stream().map(ItemStack::new));
                    ItemStack output = new ItemStack(outItem);
                    NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, input);
                    String oreId = p.first().id;
                    recipes.add(new ShapelessRecipe(new ResourceLocation(ExcavatedVariants.MOD_ID, oreId + "_conversion"), "excavated_variants.ore_conversion", output, inputs));
                }
            }
            CategoryIdentifier<Display> categoryIdentifier = CategoryIdentifier.of("minecraft", "plugins/crafting");
            for (CraftingRecipe recipe : recipes) {
                Collection<Display> displays = registry.tryFillDisplay(recipe);
                for (Display display : displays) {
                    if (Objects.equals(display.getCategoryIdentifier(), categoryIdentifier)) {
                        registry.add(display, recipe);
                    }
                }
            }
        }
    }
}
