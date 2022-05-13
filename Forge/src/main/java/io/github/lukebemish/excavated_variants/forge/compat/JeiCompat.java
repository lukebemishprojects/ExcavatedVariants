package io.github.lukebemish.excavated_variants.forge.compat;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.recipe.OreConversionRecipe;
import io.github.lukebemish.excavated_variants.util.Pair;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        if (ExcavatedVariants.getConfig().add_conversion_recipes && ExcavatedVariants.getConfig().jei_rei_compat) {
            List<CraftingRecipe> recipes = new ArrayList<>();
            OreConversionRecipe.assembleOrNull();
            for (Pair<BaseOre, HashSet<BaseStone>> p :ExcavatedVariants.oreStoneList) {
                ArrayList<Item> items = new ArrayList<>();
                for (BaseStone stone : p.last()) {
                    ResourceLocation rl = new ResourceLocation(ExcavatedVariants.MOD_ID,stone.id+"_"+p.first().id);
                    Item item = Services.REGISTRY_UTIL.getItemById(rl);
                    if (item!=null) {
                        items.add(item);
                    }
                }
                Item outItem = Services.REGISTRY_UTIL.getItemById(p.first().block_id.get(0));
                if (items.size() > 0 && outItem!=null) {
                    Ingredient input = Ingredient.of(items.stream().map(ItemStack::new));
                    ItemStack output = new ItemStack(outItem);
                    NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, input);
                    String ore_id = p.first().id;
                    recipes.add(new ShapelessRecipe(new ResourceLocation(ExcavatedVariants.MOD_ID, ore_id + "_conversion"), "excavated_variants.ore_conversion", output, inputs));
                }
            }
            registration.addRecipes(RecipeTypes.CRAFTING,recipes);
        }
    }
}
