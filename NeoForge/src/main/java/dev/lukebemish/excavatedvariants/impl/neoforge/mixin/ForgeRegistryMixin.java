/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge.mixin;

import dev.lukebemish.excavatedvariants.impl.BlockAddedCallback;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryMixin<V> {
    @Shadow
    @Final
    private ResourceKey<Registry<V>> key;

    @Inject(method = "add(ILnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;Ljava/lang/String;)I", at = @At("RETURN"))
    private void excavated_variants$registryRegisterHackery(int id, ResourceLocation rl, V value, String owner, CallbackInfoReturnable<Integer> ci) {
        if (key == (ResourceKey<? extends Registry<?>>) ForgeRegistries.Keys.BLOCKS) {
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, rl);
            BlockAddedCallback.onRegister((Block) value, blockKey);
        }
    }
}
