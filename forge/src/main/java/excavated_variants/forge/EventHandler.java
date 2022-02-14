package excavated_variants.forge;


import excavated_variants.ExcavatedVariants;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void biomeModifier(BiomeLoadingEvent event) {
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal(), () -> BuiltinRegistries.PLACED_FEATURE.get(new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer")));
        }
    }
}
