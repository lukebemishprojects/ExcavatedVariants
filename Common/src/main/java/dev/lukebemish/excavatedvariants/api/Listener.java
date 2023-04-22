package dev.lukebemish.excavatedvariants.api;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of any number of different events that can be fired by Excavated Variants, which mods can provide
 * their own implementations of to listen to. Different subclasses of this interface are used for different types of
 * events, and may have their own entrypoints to register to on Quilt; the {@link ExcavatedVariantsListener} annotation
 * can be used to automatically register implementations of this interface on Forge.
 */
public interface Listener extends Comparable<Listener> {
    default int priority() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull Listener o) {
        return o.priority() - this.priority();
    }
}
