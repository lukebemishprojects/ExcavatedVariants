/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api;

import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.ApiStatus;

public interface PostRegistrationListener extends CommonListener {

    void registriesComplete(
            Registries Registries
    );

    final class Registries {
        public final Registry<GroundType> groundTypes;
        public final Registry<Stone> stones;
        public final Registry<Ore> ores;
        public final Registry<Modifier> modifiers;

        @ApiStatus.Internal
        public Registries(Registry<GroundType> groundTypes, Registry<Stone> stones, Registry<Ore> ores, Registry<Modifier> modifiers) {
            this.groundTypes = groundTypes;
            this.stones = stones;
            this.ores = ores;
            this.modifiers = modifiers;
        }
    }
}
