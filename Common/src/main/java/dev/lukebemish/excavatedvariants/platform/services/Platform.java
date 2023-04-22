package dev.lukebemish.excavatedvariants.platform.services;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public interface Platform {
    boolean isQuilt();

    boolean isForge();

    Collection<String> getModIds();

    Path getConfigFolder();

    Path getModDataFolder();

    boolean isClient();

    /**
     * Gets a list of mining levels, ordered softest to hardest
     */
    List<ResourceLocation> getMiningLevels();
}
