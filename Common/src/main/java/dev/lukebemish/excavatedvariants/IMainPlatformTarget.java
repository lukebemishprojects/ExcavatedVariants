package dev.lukebemish.excavatedvariants;

import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public interface IMainPlatformTarget {
    <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier);

    void registerFeatures();

    ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone);
}
