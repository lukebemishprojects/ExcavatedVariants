package io.github.lukebemish.excavated_variants.quilt;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.IPlatform;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;
import java.util.Collection;

@AutoService(IPlatform.class)
public class PlatformImpl implements IPlatform {
    public boolean isQuilt() {
        return true;
    }

    public boolean isForge() {
        return false;
    }

    public Collection<String> getModIds() {
        return QuiltLoader.getAllMods().stream().map(ModContainer::metadata).map(ModMetadata::id).toList();
    }

    public Path getConfigFolder() {
        return QuiltLoader.getConfigDir().toAbsolutePath().normalize();
    }

    @Override
    public Path getModDataFolder() {
        return QuiltLoader.getGameDir().resolve("mod_data/excavated_variants").toAbsolutePath().normalize();
    }
}
