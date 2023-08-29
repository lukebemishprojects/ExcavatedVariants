/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.client;

import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.Listener;

/**
 * A listener which is fired only on the client; registered with the {@code excavated_variants_client} entrypoint or
 * through the {@link ExcavatedVariantsListener}. annotation, depending on the platform.
 */
public interface ClientListener extends Listener {
}
