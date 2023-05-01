/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import java.util.function.Supplier;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import dev.lukebemish.excavatedvariants.impl.platform.services.MainPlatformTarget;
import dev.lukebemish.excavatedvariants.impl.worldgen.OreReplacer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

@AutoService(MainPlatformTarget.class)
public class MainPlatformTargetImpl implements MainPlatformTarget {

    public void registerFeatures() {
        Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"), new OreReplacer());
    }

    public ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone) {
        return new ModifiedOreBlock(ore, stone);
    }

    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier) {
        RecipeSerializer<T> out = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(ExcavatedVariants.MOD_ID, name), supplier.get());
        return () -> out;
    }
}
