package io.github.lukebemish.excavated_variants.forge;

import io.github.lukebemish.excavated_variants.IPlatform;
import com.google.auto.service.AutoService;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.Collection;

@AutoService(IPlatform.class)
public class PlatformImpl implements IPlatform {
    public boolean isFabric() {
        return false;
    }

    public boolean isForge() {
        return true;
    }

    public Collection<String> getModIds() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
}
