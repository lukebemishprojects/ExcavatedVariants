/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TagQueue implements Supplier<Map<ResourceLocation, Set<ResourceLocation>>> {
    private final Map<ResourceLocation, Set<ResourceLocation>> queue = new HashMap<>();

    public void queue(ResourceLocation tag, ResourceLocation entry) {
        queue.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
    }

    @Override
    public Map<ResourceLocation, Set<ResourceLocation>> get() {
        Map<ResourceLocation, Set<ResourceLocation>> out = new HashMap<>();

        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : queue.entrySet()) {
            Set<ResourceLocation> set = new HashSet<>();
            for (ResourceLocation rl : entry.getValue()) {
                if (entry.getKey().getPath().startsWith("blocks/")) {
                    if (BuiltInRegistries.BLOCK.containsKey(rl)) {
                        set.add(rl);
                    }
                } else if (entry.getKey().getPath().startsWith("items/")) {
                    if (BuiltInRegistries.ITEM.containsKey(rl)) {
                        set.add(rl);
                    }
                } else {
                    set.add(rl);
                }
            }
            out.put(entry.getKey(), entry.getValue());
        }

        return out;
    }
}
