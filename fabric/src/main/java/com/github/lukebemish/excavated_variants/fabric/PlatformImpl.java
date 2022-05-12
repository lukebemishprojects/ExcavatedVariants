package com.github.lukebemish.excavated_variants.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class PlatformImpl {
    public static boolean isFabric() {
        return true;
    }

    public static boolean isForge() {
        return false;
    }

    public static Collection<String> getModIds() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toList());
    }

    public static Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().normalize();
    }
}
