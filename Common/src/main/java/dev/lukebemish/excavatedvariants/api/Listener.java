/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api;

import org.jspecify.annotations.NonNull;

/**
 * An implementation of any number of different events that can be fired by Excavated Variants, which mods can provide
 * their own implementations of to listen to. Different subclasses of this interface are used for different types of
 * events, and may be registered either with specific entrypoints or the {@link ExcavatedVariantsListener} annotation,
 * depending on the platform.
 */
public interface Listener extends Comparable<Listener> {
    default int priority() {
        return 0;
    }

    @Override
    default int compareTo(@NonNull Listener o) {
        return o.priority() - this.priority();
    }
}
