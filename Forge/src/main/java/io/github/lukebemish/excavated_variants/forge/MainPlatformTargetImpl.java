package io.github.lukebemish.excavated_variants.forge;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.IMainPlatformTarget;
import io.github.lukebemish.excavated_variants.ModifiedOreBlock;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

@AutoService(IMainPlatformTarget.class)
public class MainPlatformTargetImpl implements IMainPlatformTarget {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registry.RECIPE_SERIALIZER_REGISTRY, ExcavatedVariants.MOD_ID);

    public void registerFeatures() {
    }

    public ModifiedOreBlock makeDefaultOreBlock(BaseOre ore, BaseStone stone) {
        return new ForgeOreBlock(ore, stone);
    }

    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier) {
        return RECIPE_SERIALIZERS.register(name, supplier);
    }
}
