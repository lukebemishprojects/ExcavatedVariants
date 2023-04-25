package dev.lukebemish.excavatedvariants.impl.platform.services;

import java.util.function.Supplier;

import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface MainPlatformTarget {
    <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier);

    void registerFeatures();

    ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone);
}
