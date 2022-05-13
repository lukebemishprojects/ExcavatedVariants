package io.github.lukebemish.excavated_variants;

import java.nio.file.Path;
import java.util.Collection;

public interface IPlatform {
    boolean isFabriquilt();
    boolean isForge();
    Collection<String> getModIds();
    Path getConfigFolder();
}
