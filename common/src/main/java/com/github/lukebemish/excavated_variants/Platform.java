package com.github.lukebemish.excavated_variants;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;
import java.util.Collection;

public class Platform {
    @ExpectPlatform
    public static boolean isFabric() {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static boolean isForge() {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static Collection<String> getModIds() {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static Path getConfigFolder() {
        throw new AssertionError();
    }
}
