package dev.lukebemish.excavatedvariants.quilt;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.IMainPlatformTarget;
import dev.lukebemish.excavatedvariants.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.worldgen.OreReplacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

@AutoService(IMainPlatformTarget.class)
public class MainPlatformTargetImpl implements IMainPlatformTarget {

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
