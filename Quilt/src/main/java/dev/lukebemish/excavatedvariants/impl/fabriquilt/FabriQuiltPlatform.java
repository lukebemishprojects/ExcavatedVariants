package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric.FabricPlatform;
import dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt.QuiltPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Set;

public interface FabriQuiltPlatform {
    boolean isQuilt();

    @SuppressWarnings("deprecation")
    static FabriQuiltPlatform getInstance() {
        if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
            return QuiltPlatform.INSTANCE;
        } else {
            return FabricPlatform.INSTANCE;
        }
    }

    Path getCacheFolder();
    String getModVersion();
    Set<String> getModIds();
}
