package dev.lukebemish.excavatedvariants.platform;

import java.util.ServiceLoader;

import dev.lukebemish.excavatedvariants.platform.services.Platform;
import dev.lukebemish.excavatedvariants.platform.services.CreativeTabLoader;
import dev.lukebemish.excavatedvariants.platform.services.Listener;
import dev.lukebemish.excavatedvariants.platform.services.MainPlatformTarget;
import dev.lukebemish.excavatedvariants.platform.services.RegistryUtil;
import dev.lukebemish.excavatedvariants.util.ThreadsafeLazy;

public class Services {
    public static final RegistryUtil REGISTRY_UTIL = load(RegistryUtil.class);
    public static final Platform PLATFORM = load(Platform.class);
    public static final ThreadsafeLazy<CreativeTabLoader> CREATIVE_TAB_LOADER = loadLazy(CreativeTabLoader.class);
    public static final ThreadsafeLazy<MainPlatformTarget> MAIN_PLATFORM_TARGET = loadLazy(MainPlatformTarget.class);
    public static final Listener COMPAT = load(Listener.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    public static <T> ThreadsafeLazy<T> loadLazy(Class<T> clazz) {
        return new ThreadsafeLazy<>(() -> load(clazz));
    }
}
