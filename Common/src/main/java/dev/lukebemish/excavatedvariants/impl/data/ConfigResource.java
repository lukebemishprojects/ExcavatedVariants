/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.data.filter.VariantFilter;
import dev.lukebemish.excavatedvariants.impl.data.filter.ObjectVariantFilter;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ConfigResource {
    public static final Codec<ConfigResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariantFilter.CODEC.optionalFieldOf("blacklist", ObjectVariantFilter.EmptyVariantFilter.INSTANCE).forGetter(r -> r.blacklist),
            ResourceLocation.CODEC.listOf().optionalFieldOf("priority", List.of()).forGetter(r -> r.priority)
    ).apply(instance, ConfigResource::new));
    public final List<ResourceLocation> priority;
    private VariantFilter blacklist;

    private ConfigResource(VariantFilter blacklist, List<ResourceLocation> priority) {
        this.blacklist = blacklist;
        this.priority = new ArrayList<>(priority);
    }

    public static ConfigResource empty() {
        return new ConfigResource(ObjectVariantFilter.EmptyVariantFilter.INSTANCE, List.of());
    }

    private static <T> void addAllNew(List<? super T> to, List<? extends T> from) {
        from.stream().filter(it->!to.contains(it)).forEach(to::add);
    }

    public VariantFilter getBlacklist() {
        return blacklist;
    }

    public void addFrom(ConfigResource resource) {
        this.blacklist = VariantFilter.union(this.blacklist, resource.blacklist);
        addAllNew(this.priority, resource.priority);
    }
}
