/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.Resettable;
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache;
import dev.lukebemish.excavatedvariants.impl.client.ItemModelPlanner;
import dev.lukebemish.excavatedvariants.impl.client.ResourceAssembler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcavatedVariantsClient {
    public static final LangBuilder LANG_BUILDER = new LangBuilder();

    public static final AssetResourceCache ASSET_CACHE = ResourceCache.register(new AssetResourceCache(new ResourceLocation(ExcavatedVariants.MOD_ID, "assets")));

    public static final ItemModelPlanner ITEM_MODEL_PLANNER = new ItemModelPlanner();

    public static void init() {
        // lang - don't bother caching
        ASSET_CACHE.planSource(new PathAwareInputStreamSource() {
            @Override
            public Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
                return LANG_BUILDER.languages().stream().map(s -> new ResourceLocation(ExcavatedVariants.MOD_ID+"_generated", "lang/" + s + ".json")).collect(Collectors.toSet());
            }

            @Override
            public IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
                return LANG_BUILDER.build(outRl.getPath().substring(5, outRl.getPath().length() - 5));
            }
        });
        class ResourceAssemblerSource implements PathAwareInputStreamSource, Resettable {
            @Nullable ResourceAssembler assembler = null;

            private synchronized void setup(ResourceGenerationContext context) {
                if (assembler == null) {
                    assembler = new ResourceAssembler();
                    for (var future : ExcavatedVariants.COMPLETE_VARIANTS) {
                        assembler.addFuture(future, context);
                    }
                }
            }

            @Override
            public @NonNull Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
                setup(context);
                return assembler.getLocations(context);
            }

            @Override
            public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
                setup(context);
                return assembler.get(outRl, context);
            }

            @Override
            public @Nullable String createCacheKey(ResourceLocation outRl, ResourceGenerationContext context) {
                setup(context);
                return assembler.createCacheKey(outRl, context);
            }

            @Override
            public void reset(ResourceGenerationContext context) {
                assembler = null;
            }
        }
        ASSET_CACHE.planSource(new ResourceAssemblerSource());
        ASSET_CACHE.planSource(ITEM_MODEL_PLANNER);
    }

    public static void setUp(ExcavatedVariants.VariantFuture future) {
        ITEM_MODEL_PLANNER.add(future.fullId);
        LANG_BUILDER.add(future.fullId, future.stone, future.ore);
    }
}
