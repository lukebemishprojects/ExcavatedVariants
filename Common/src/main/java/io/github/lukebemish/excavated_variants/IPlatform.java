package io.github.lukebemish.excavated_variants;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface IPlatform {
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
