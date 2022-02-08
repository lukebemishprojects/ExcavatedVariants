package excavated_variants.fabric;

import net.fabricmc.api.ClientModInitializer;
import excavated_variants.ExcavatedVariantsClient;

public class ExcavatedVariantsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExcavatedVariantsClient.init();
    }
}
