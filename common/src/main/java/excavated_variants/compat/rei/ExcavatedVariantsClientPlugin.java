package excavated_variants.compat.rei;

import excavated_variants.ExcavatedVariants;
import excavated_variants.Pair;
import excavated_variants.RegistryUtil;
import excavated_variants.data.BaseOre;
import excavated_variants.data.BaseStone;
import excavated_variants.recipe.OreConversionRecipe;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ExcavatedVariantsClientPlugin implements REIClientPlugin {
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        if (ExcavatedVariants.getConfig().add_conversion_recipes) {
            List<CraftingRecipe> recipes = new ArrayList<>();
            OreConversionRecipe.assembleOrNull();
            for (Pair<BaseOre, List<BaseStone>> p : ExcavatedVariants.oreStoneList) {
                ArrayList<Item> items = new ArrayList<>();
                for (BaseStone stone : p.last()) {
                    ResourceLocation rl = new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + p.first().id);
                    Item item = RegistryUtil.getItemById(rl);
                    if (item != null) {
                        items.add(item);
                    }
                }
                Item outItem = RegistryUtil.getItemById(p.first().rl_block_id.get(0));
                if (items.size() > 0 && outItem != null) {
                    Ingredient input = Ingredient.of(items.stream().map(ItemStack::new));
                    ItemStack output = new ItemStack(outItem);
                    NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, input);
                    String ore_id = p.first().id;
                    recipes.add(new ShapelessRecipe(new ResourceLocation(ExcavatedVariants.MOD_ID, ore_id + "_conversion"), "excavated_variants.ore_conversion", output, inputs));
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
