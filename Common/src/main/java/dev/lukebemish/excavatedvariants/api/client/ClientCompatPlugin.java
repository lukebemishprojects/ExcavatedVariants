package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.NotNull;

public interface ClientCompatPlugin extends Comparable<ClientCompatPlugin> {
    default int priority() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull ClientCompatPlugin o) {
        return o.priority() - this.priority();
    }
}
