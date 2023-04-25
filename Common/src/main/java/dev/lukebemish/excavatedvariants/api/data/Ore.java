/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Ore {
    private final BaseOre ore;

    @ApiStatus.Internal
    public Ore(@NotNull BaseOre ore) {
        this.ore = ore;
    }

    public String getId() {
        return ore.id;
    }

    public Map<String, String> getTranslations() {
        return Collections.unmodifiableMap(ore.lang);
    }

    public List<String> getOreNames() {
        return Collections.unmodifiableList(ore.oreName);
    }

    public List<String> getStones() {
        return Collections.unmodifiableList(ore.stone);
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(ore.types);
    }

    public List<ResourceLocation> getBlockIds() {
        return Collections.unmodifiableList(ore.blockId);
    }

    @ApiStatus.Internal
    public BaseOre getBase() {
        return ore;
    }
}
