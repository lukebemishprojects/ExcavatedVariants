package excavated_variants.fabric;

import excavated_variants.ExcavatedVariants;
import net.fabricmc.api.ModInitializer;

public class ExcavatedVariantsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExcavatedVariants.init();
    }
}
