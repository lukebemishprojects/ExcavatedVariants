/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.platform;

import dev.lukebemish.excavatedvariants.impl.platform.services.CreativeTabLoader;
import dev.lukebemish.excavatedvariants.impl.platform.services.Platform;
import dev.lukebemish.excavatedvariants.impl.util.ThreadsafeLazy;

import java.util.List;
import java.util.ServiceLoader;

public class Services {
    public static final Platform PLATFORM = load(Platform.class);
    public static final ThreadsafeLazy<CreativeTabLoader> CREATIVE_TAB_LOADER = loadLazy(CreativeTabLoader.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    public static <T> List<T> loadListeners(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .stream()
                .map(ServiceLoader.Provider::get)
                .sorted()
                .toList();
    }

    public static <T> ThreadsafeLazy<T> loadLazy(Class<T> clazz) {
        return new ThreadsafeLazy<>(() -> load(clazz));
    }
}
