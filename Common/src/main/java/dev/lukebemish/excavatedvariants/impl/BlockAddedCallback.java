/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.impl.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class BlockAddedCallback {

    private static boolean registering = false;
    private static boolean ready = false;

    public static void setReady() {
        ready = true;
    }

    public static void register() {
        if (ready && ModLifecycle.getState() == ModLifecycle.State.REGISTRATION && !registering) {
            registering = true;
            ExcavatedVariants.VariantFuture future;
            while ((future = ExcavatedVariants.READY_QUEUE.poll()) != null) {
                Services.PLATFORM.register(future);
            }
            registering = false;
        }
    }

    public static void onRegister(Block value, ResourceKey<Block> blockKey) {
        if (ModLifecycle.getState() != ModLifecycle.State.REGISTRATION) {
            return;
        }
        List<ExcavatedVariants.VariantFuture> futures = ExcavatedVariants.NEEDED_KEYS.get(blockKey);
        if (futures != null) {
            Map<ExcavatedVariants.VariantFuture, List<ResourceKey<Block>>> toRemove = new IdentityHashMap<>();
            for (ExcavatedVariants.VariantFuture future : futures) {
                if (blockKey == future.stone.block && future.foundStone == null) {
                    future.foundStone = value;
                } else if (future.foundOre == null) {
                    future.foundOre = value;
                    future.foundOreKey = blockKey;
                    ResourceKey<Stone> sourceStoneKey = future.ore.getBlocks().get(blockKey);
                    future.foundSourceStone = Objects.requireNonNull(RegistriesImpl.STONE_REGISTRY.get(sourceStoneKey), "No such stone: "+sourceStoneKey.location());
                }
                if (future.foundOre != null && future.foundStone != null) {
                    ExcavatedVariants.READY_QUEUE.add(future);
                    toRemove.put(future, ExcavatedVariants.REVERSE_NEEDED_KEYS.get(future));
                }
            }
            if (!toRemove.isEmpty()) {
                List<ResourceKey<Block>> toRemoveKeys = new ArrayList<>();
                for (var entry : toRemove.entrySet()) {
                    for (var key : entry.getValue()) {
                        var list = ExcavatedVariants.NEEDED_KEYS.get(key);
                        if (list == null) continue;
                        list.remove(entry.getKey());
                        if (list.isEmpty()) {
                            toRemoveKeys.add(key);
                        }
                    }
                }
                if (!toRemoveKeys.isEmpty()) {
                    for (var key : toRemoveKeys) {
                        ExcavatedVariants.NEEDED_KEYS.remove(key);
                    }
                }
            }
            BlockAddedCallback.register();
            ExcavatedVariants.NEEDED_KEYS.remove(blockKey);
        }
    }
}
