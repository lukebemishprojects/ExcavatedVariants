package dev.lukebemish.excavatedvariants.compat.jei;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.platform.Services;
import dev.lukebemish.excavatedvariants.recipe.OreConversionRecipe;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@JeiPlugin
public class JeiCompat implements IModPlugin {

    @Override
    @NotNull
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ExcavatedVariants.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (ExcavatedVariants.getConfig().addConversionRecipes && ExcavatedVariants.getConfig().jeiReiCompat) {
            List<CraftingRecipe> recipes = new ArrayList<>();
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
                    recipes.add(new ShapelessRecipe(new ResourceLocation(ExcavatedVariants.MOD_ID, oreId + "_conversion"), "excavated_variants.ore_conversion", CraftingBookCategory.MISC, output, inputs));
                }
            }
            registration.addRecipes(RecipeTypes.CRAFTING, recipes);
        }
    }
}
