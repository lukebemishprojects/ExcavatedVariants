/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt;

import com.google.auto.service.AutoService;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.platform.services.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {

    @Override
    public Set<String> getModIds() {
        return FabriQuiltPlatform.getInstance().getModIds();
    }

    public Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getModDataFolder() {
        return FabriQuiltPlatform.getInstance().getCacheFolder().resolve(ExcavatedVariants.MOD_ID);
    }

    @Override
    public String getModVersion() {
        return FabriQuiltPlatform.getInstance().getModVersion();
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public List<ResourceLocation> getMiningLevels(ResourceGenerationContext context) {
        List<ResourceLocation> out = new ArrayList<>();
        out.add(BlockTags.NEEDS_STONE_TOOL.location());
        out.add(BlockTags.NEEDS_IRON_TOOL.location());
        out.add(BlockTags.NEEDS_DIAMOND_TOOL.location());
        int maxLevel = context.getResourceSource().listResources("tags/blocks", rl -> {
            if (!rl.getNamespace().equals("fabric")) return false;
            if (!rl.getPath().startsWith("needs_tool_level_")) return false;
            if (!rl.getPath().endsWith(".json")) return false;
            String tagName = rl.getPath().substring("needs_tool_level_".length(), rl.getPath().length() - ".json".length());
            try {
                Integer.parseInt(tagName);
            } catch (NumberFormatException ignored) {
                return false;
            }
            return true;
        }).keySet().stream().mapToInt(rl -> {
            String partial = rl.getPath().substring("needs_tool_level_".length(), rl.getPath().length() - ".json".length());
            return Integer.parseInt(partial);
        }).max().orElse(0);
        if (maxLevel >= 4) {
            for (int i = 4; i <= maxLevel; i++) {
                out.add(MiningLevelManager.getBlockTag(i).location());
            }
        }
        return out.stream().map(it->it.withPrefix("blocks/")).toList();
    }

    @Override
    public void register(ExcavatedVariants.VariantFuture future) {
        ExcavatedVariants.registerBlockAndItem(
                (rlr, bl) -> Registry.register(BuiltInRegistries.BLOCK, rlr, bl),
                (rlr, it) -> {
                    Item out = Registry.register(BuiltInRegistries.ITEM, rlr, it.get());
                    return () -> out;
                }, future);
    }

}
