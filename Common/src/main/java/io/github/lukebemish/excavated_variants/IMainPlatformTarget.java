package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public interface IMainPlatformTarget {
    <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier);

    void registerFeatures();

    ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone);
}
