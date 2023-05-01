/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.util;

import java.util.function.Supplier;

public class ThreadsafeLazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private volatile T cache;

    public ThreadsafeLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = supplier.get();
                }
            }
        }
        return cache;
    }
}
