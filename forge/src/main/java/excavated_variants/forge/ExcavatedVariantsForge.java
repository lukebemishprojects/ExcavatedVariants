package excavated_variants.forge;

import dev.architectury.platform.forge.EventBuses;
import excavated_variants.ExcavatedVariantsClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import excavated_variants.ExcavatedVariants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExcavatedVariants.MOD_ID)
public class ExcavatedVariantsForge {
    public ExcavatedVariantsForge() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(ExcavatedVariants.MOD_ID, modbus);
        ExcavatedVariants.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ExcavatedVariantsClient::init);
    }
}
