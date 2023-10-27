/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt;

import com.google.common.base.Suppliers;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.fabriquilt.FabriQuiltPlatform;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QuiltPlatform implements FabriQuiltPlatform {
    private QuiltPlatform() {}

    public static final QuiltPlatform INSTANCE = new QuiltPlatform();
    private static final Supplier<Set<String>> MOD_IDS = Suppliers.memoize(() -> QuiltLoader.getAllMods().stream().map(ModContainer::metadata).map(ModMetadata::id).collect(Collectors.toSet()));
    @Override
    public Set<String> getModIds() {
        return MOD_IDS.get();
    }

    @Override
    public Path getCacheFolder() {
        return QuiltLoader.getCacheDir();
    }

    @Override
    public String getModVersion() {
        return QuiltLoader.getModContainer(ExcavatedVariants.MOD_ID).orElseThrow().metadata().version().raw();
    }
}
