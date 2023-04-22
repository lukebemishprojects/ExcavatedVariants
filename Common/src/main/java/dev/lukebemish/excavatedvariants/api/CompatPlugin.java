package dev.lukebemish.excavatedvariants.api;

import org.jetbrains.annotations.NotNull;

public interface CompatPlugin extends Comparable<CompatPlugin> {
    default int priority() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull CompatPlugin o) {
        return o.priority() - this.priority();
    }
}
