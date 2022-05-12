package io.github.lukebemish.excavated_variants.platform;

import io.github.lukebemish.excavated_variants.*;
import io.github.lukebemish.excavated_variants.*;

import java.util.ServiceLoader;

public class Services {
    public static final IRegistryUtil REGISTRY_UTIL = load(IRegistryUtil.class);
    public static final IMainPlatformTarget MAIN_PLATFORM_TARGET = load(IMainPlatformTarget.class);
    public static final IPlatform PLATFORM = load(IPlatform.class);
    public static final ICreativeTabLoader CREATIVE_TAB_LOADER = load(ICreativeTabLoader.class);
    public static final ICompat COMPAT = load(ICompat.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
