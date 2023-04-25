package dev.lukebemish.excavatedvariants.impl.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.platform.services.MainPlatformTarget;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

@AutoService(MainPlatformTarget.class)
public class MainPlatformTargetImpl implements MainPlatformTarget {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExcavatedVariants.MOD_ID);

    public void registerFeatures() {
    }

    public ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone) {
        return new ForgeOreBlock(ore, stone);
    }

    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier) {
        return RECIPE_SERIALIZERS.register(name, supplier);
    }
}
