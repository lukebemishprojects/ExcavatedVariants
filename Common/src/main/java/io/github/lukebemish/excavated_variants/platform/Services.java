package io.github.lukebemish.excavated_variants.platform;

import io.github.lukebemish.excavated_variants.*;
import io.github.lukebemish.excavated_variants.util.ThreadsafeLazy;

import java.util.ServiceLoader;

public class Services {
    public static final IRegistryUtil REGISTRY_UTIL = load(IRegistryUtil.class);
    public static final IPlatform PLATFORM = load(IPlatform.class);
    public static final ThreadsafeLazy<ICreativeTabLoader> CREATIVE_TAB_LOADER = loadLazy(ICreativeTabLoader.class);
    public static final ThreadsafeLazy<IMainPlatformTarget> MAIN_PLATFORM_TARGET = loadLazy(IMainPlatformTarget.class);
    public static final ICompat COMPAT = load(ICompat.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    public static <T> ThreadsafeLazy<T> loadLazy(Class<T> clazz) {
        return new ThreadsafeLazy<>(()->load(clazz));
    }
}
