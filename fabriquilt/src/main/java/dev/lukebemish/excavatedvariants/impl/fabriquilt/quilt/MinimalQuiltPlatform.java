/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt;

import dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric.FabricPlatform;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class MinimalQuiltPlatform extends FabricPlatform {
    private MinimalQuiltPlatform() {}

    public static final MinimalQuiltPlatform INSTANCE = new MinimalQuiltPlatform();

    @Override
    public Path getCacheFolder() {
        return QuiltLoader.getCacheDir();
    }
}
