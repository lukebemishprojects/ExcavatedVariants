/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class OreBuilder {
    private String id;
    private final List<String> oreName = new ArrayList<>();
    private final List<String> stones = new ArrayList<>();
    private final List<ResourceLocation> blockIds = new ArrayList<>();
    private final Map<String, String> translations = new HashMap<>();
    private final List<String> types = new ArrayList<>();

    @ApiStatus.Internal
    public Ore build() {
        if (id == null) {
            throw new IllegalStateException("Ore ID must be set");
        }
        if (oreName.isEmpty()) {
            oreName.add(id);
        }

        return new Ore(new BaseOre(id, Optional.of(Either.right(oreName)), stones, blockIds, Optional.empty(), types, translations));
    }

    public OreBuilder id(String id) {
        if (this.id != null)
            throw new IllegalStateException("Ore ID already set");
        this.id = id;
        return this;
    }

    public OreBuilder oreNames(String... oreName) {
        this.oreName.addAll(Arrays.asList(oreName));
        return this;
    }

    public OreBuilder oreNames(List<String> oreName) {
        this.oreName.addAll(oreName);
        return this;
    }

    public OreBuilder stones(String... stones) {
        this.stones.addAll(Arrays.asList(stones));
        return this;
    }

    public OreBuilder stones(List<String> stones) {
        this.stones.addAll(stones);
        return this;
    }

    public OreBuilder blockIds(ResourceLocation... blockIds) {
        this.blockIds.addAll(Arrays.asList(blockIds));
        return this;
    }

    public OreBuilder blockIds(List<ResourceLocation> blockIds) {
        this.blockIds.addAll(blockIds);
        return this;
    }

    public OreBuilder translations(Map<String, String> translations) {
        this.translations.putAll(translations);
        return this;
    }

    public OreBuilder types(String... types) {
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    public OreBuilder types(List<String> types) {
        this.types.addAll(types);
        return this;
    }
}
