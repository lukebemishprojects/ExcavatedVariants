package io.github.lukebemish.excavated_variants;

import java.nio.file.Path;
import java.util.Collection;

public interface IPlatform {
    boolean isQuilt();

    boolean isForge();

    Collection<String> getModIds();

    Path getConfigFolder();

    Path getModDataFolder();
}
