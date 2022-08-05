package io.github.lukebemish.excavated_variants.util;

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
