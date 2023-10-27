/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric;

import com.google.common.base.Suppliers;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.fabriquilt.FabriQuiltPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public final class FabricPlatform implements FabriQuiltPlatform {
    public static final FabricPlatform INSTANCE = new FabricPlatform();
    private static final Supplier<Set<String>> MOD_IDS = Suppliers.memoize(() -> FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet()));
    private FabricPlatform() {}

    @Override
    public Path getCacheFolder() {
        return FabricLoader.getInstance().getGameDir().resolve(".cache");
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(ExcavatedVariants.MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public Set<String> getModIds() {
        return MOD_IDS.get();
    }
}
