package dev.lukebemish.excavatedvariants.quilt.compat.emi;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.platform.Services;
import dev.lukebemish.excavatedvariants.recipe.OreConversionRecipe;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EmiCompat implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (ExcavatedVariants.getConfig().addConversionRecipes && ExcavatedVariants.getConfig().jeiReiCompat) {
            List<RecipeHolder> recipes = new ArrayList<>();
            OreConversionRecipe.assembleOrNull();
            for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                ArrayList<Item> items = new ArrayList<>();
                for (BaseStone stone : p.getSecond()) {
                    ResourceLocation rl = new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + p.getFirst().id);
                    Item item = Services.REGISTRY_UTIL.getItemById(rl);
                    if (item != null) {
                        items.add(item);
                    }
                }
                Item outItem = Services.REGISTRY_UTIL.getItemById(p.getFirst().blockId.get(0));
                if (!items.isEmpty() && outItem != null) {
                    Ingredient input = Ingredient.of(items.stream().map(ItemStack::new));
                    ItemStack output = new ItemStack(outItem);
                    NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, input);
                    String oreId = p.getFirst().id;
                    recipes.add(new RecipeHolder(inputs, output, new ResourceLocation(ExcavatedVariants.MOD_ID, oreId + "_conversion")));
                }
            }
            for (RecipeHolder recipe : recipes) {
                registry.addRecipe(new EmiCraftingRecipe(recipe.input().stream().map(EmiIngredient::of).toList(),
                        EmiStack.of(recipe.output()),recipe.id()));
            }
        }
    }

    private record RecipeHolder(List<Ingredient> input, ItemStack output, ResourceLocation id) {

    }
}
