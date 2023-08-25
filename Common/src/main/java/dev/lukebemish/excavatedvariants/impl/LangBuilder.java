/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LangBuilder {
    private static final Codec<Map<String,String>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);
    private final Map<String, Map<String, String>> internal = new HashMap<>();

    public void add(String fullId, Stone stone, Ore ore) {
        Set<String> langs = new HashSet<>(stone.translation.keySet());
        langs.addAll(ore.translation.keySet());
        for (String langName : langs) {
            String stoneLang = stone.translation.getOrDefault(langName,stone.getKeyOrThrow().location().toLanguageKey("excavated_variants.stone"));
            String oreLang = ore.translation.getOrDefault(langName,ore.getKeyOrThrow().location().toLanguageKey("excavated_variants.ore"));
            String name = oreLang.contains("%s") ? oreLang.replaceFirst("%s", stoneLang) : stoneLang + " " + oreLang;
            internal.computeIfAbsent(langName, k -> new HashMap<>()).put("block."+ExcavatedVariants.MOD_ID+"."+fullId,name);
        }
    }

    public void add(String key, String name) {
        internal.computeIfAbsent("en_us", k -> new HashMap<>()).put(key,name);
    }

    public Set<String> languages() {
        return internal.keySet();
    }

    public IoSupplier<InputStream> build(String langName) {
        String json = ExcavatedVariants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal.getOrDefault(langName, Map.of())).getOrThrow(false, e->{}));
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
