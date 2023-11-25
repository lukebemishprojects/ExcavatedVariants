/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.fabriquilt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DimensionDataStorage.class)
public class DimensionDataStorageMixin {
    @WrapOperation(
            method = "readTagFromDisk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag excavated_variants$readTagFromDisk(DataFixTypes dataFixTypes, DataFixer fixer, CompoundTag tag, int version, int newVersion, Operation<CompoundTag> operation) {
        if (dataFixTypes == null) {
            return tag;
        } else {
            return operation.call(dataFixTypes, fixer, tag, version, newVersion);
        }
    }
}
