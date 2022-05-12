package io.github.lukebemish.excavated_variants.fabric;

import io.github.lukebemish.excavated_variants.IPlatform;
import com.google.auto.service.AutoService;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

@AutoService(IPlatform.class)
public class PlatformImpl implements IPlatform {
    public boolean isFabric() {
        return true;
    }

    public boolean isForge() {
        return false;
    }

    public Collection<String> getModIds() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toList());
    }

    public Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().normalize();
    }
}
