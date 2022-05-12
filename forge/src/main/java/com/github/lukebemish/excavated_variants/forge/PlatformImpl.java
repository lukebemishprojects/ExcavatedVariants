package com.github.lukebemish.excavated_variants.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.Collection;

public class PlatformImpl {
    public static boolean isFabric() {
        return false;
    }

    public static boolean isForge() {
        return true;
    }

    public static Collection<String> getModIds() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    public static Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
}
