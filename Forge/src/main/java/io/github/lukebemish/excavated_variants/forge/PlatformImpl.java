package io.github.lukebemish.excavated_variants.forge;

import com.google.auto.service.AutoService;
import io.github.lukebemish.excavated_variants.IPlatform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.Collection;

@AutoService(IPlatform.class)
public class PlatformImpl implements IPlatform {
    public boolean isQuilt() {
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

    @Override
    public Path getModDataFolder() {
        return FMLPaths.GAMEDIR.get().resolve("mod_data/excavated_variants");
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }
}
