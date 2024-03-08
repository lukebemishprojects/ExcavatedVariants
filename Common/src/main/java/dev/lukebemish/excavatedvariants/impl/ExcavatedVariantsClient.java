/*
 * Copyright (C) 2023-2024 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache;
import dev.lukebemish.excavatedvariants.impl.client.ItemModelPlanner;
import dev.lukebemish.excavatedvariants.impl.client.ResourceAssembler;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import dev.lukebemish.excavatedvariants.impl.data.ModData;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public final class ExcavatedVariantsClient {
    private ExcavatedVariantsClient() {}

    public static final LangBuilder LANG_BUILDER = new LangBuilder();

    public static final AssetResourceCache ASSET_CACHE = ResourceCache.register(new AssetResourceCache(new ResourceLocation(ExcavatedVariants.MOD_ID, "assets")));

    public static void init() {
        Collection<String> modids = Services.PLATFORM.getModIds();

        ExcavatedVariants.setupMap();

        Map<String, BaseStone> stoneMap = new HashMap<>();
        for (ModData mod : ExcavatedVariants.getConfig().mods) {
            if (modids.containsAll(mod.modId)) {
                for (BaseStone stone : mod.providedStones) {
                    stoneMap.put(stone.id, stone);
                }
            }
        }

        Map<BaseOre, BaseStone> originalPairs = ExcavatedVariants.oreStoneList.stream().flatMap(p -> p.getSecond().stream().map(
                stone -> new Pair<>(p.getFirst(), stoneMap.get(p.getFirst().stone.get(0))))).collect(Collectors.toMap(
                Pair::getFirst, Pair::getSecond, (a, b) -> a
        ));
        List<Pair<BaseOre, BaseStone>> toMake = new ArrayList<>();

        ItemModelPlanner planner = new ItemModelPlanner();
        ASSET_CACHE.planResetListener(planner);
        ASSET_CACHE.planSource(planner);

        for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
            var ore = p.getFirst();
            for (BaseStone stone : p.getSecond()) {
                String fullId = stone.id + "_" + ore.id;
                toMake.add(new Pair<>(ore, stone));
                planner.add(fullId);
                LANG_BUILDER.add(fullId, stone, ore);
            }
        }

        ASSET_CACHE.planSource(new PathAwareInputStreamSource() {
            @Override
            public @NotNull Set<ResourceLocation> getLocations() {
                return LANG_BUILDER.languages().stream().map(s -> new ResourceLocation(ExcavatedVariants.MOD_ID+"_generated", "lang/" + s + ".json")).collect(Collectors.toSet());
            }

            @Override
            public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
                return LANG_BUILDER.build(outRl.getPath().substring(5, outRl.getPath().length() - 5));
            }
        });

        ASSET_CACHE.planSource(() -> {
            var source = new ResourceAssembler(originalPairs, toMake);
            source.assemble();
            return source;
        });
    }

    static void planLang(String key, String enName) {
        LANG_BUILDER.add(key, enName);
    }
}
