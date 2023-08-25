/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge.mixin;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.forge.registry.BlockAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryMixin<V> {
    @Shadow
    @Final
    private ResourceKey<Registry<V>> key;

    @Inject(method = "register(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)V", at = @At("RETURN"))
    private void excavated_variants$registryRegisterHackery(ResourceLocation rl, V value, CallbackInfo ci) {
        if (key.equals(ForgeRegistries.Keys.BLOCKS)) {
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, rl);
            List<ExcavatedVariants.RegistryFuture> futures = ExcavatedVariants.NEEDED_KEYS.get(blockKey);
            if (futures != null) {
                Map<ExcavatedVariants.RegistryFuture, List<ResourceKey<Block>>> toRemove = new IdentityHashMap<>();
                for (ExcavatedVariants.RegistryFuture future : futures) {
                    if (blockKey == future.stone.block) {
                        future.foundStone = true;
                    } else {
                        future.foundOre = true;
                    }
                    if (future.foundOre && future.foundStone) {
                        ExcavatedVariants.READY_QUEUE.add(future);
                        toRemove.put(future, ExcavatedVariants.REVERSE_NEEDED_KEYS.get(future));
                    }
                }
                if (!toRemove.isEmpty()) {
                    List<ResourceKey<Block>> toRemoveKeys = new ArrayList<>();
                    for (var entry : toRemove.entrySet()) {
                        for (var key : entry.getValue()) {
                            var list = ExcavatedVariants.NEEDED_KEYS.get(key);
                            list.remove(entry.getKey());
                            toRemoveKeys.add(key);
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
}
