/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric.FabricPlatform;
import dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt.MinimalQuiltPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Set;

public interface FabriQuiltPlatform {

    @SuppressWarnings("deprecation")
    static FabriQuiltPlatform getInstance() {
        if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
            return MinimalQuiltPlatform.INSTANCE;
        } else {
            return FabricPlatform.INSTANCE;
        }
    }

    Path getCacheFolder();
    String getModVersion();
    Set<String> getModIds();
}
