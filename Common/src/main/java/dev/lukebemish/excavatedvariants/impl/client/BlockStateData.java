/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.impl.ModifiedOreBlock;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class BlockStateData {
    public static final Codec<BlockStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, VariantAssembler.CODEC.listOf()).fieldOf("variants").forGetter(BlockStateData::getVariants)
    ).apply(instance, BlockStateData::new));

    private final Map<String, List<VariantAssembler>> variants = new HashMap<>();

    public Map<String, List<VariantAssembler>> getVariants() {
        return variants;
    }

    public BlockStateData() {}
    public BlockStateData(Map<String, List<VariantAssembler>> variants) {
        this.variants.putAll(variants);
    }

    public static BlockStateData create(ModifiedOreBlock block, List<ResourceLocation> modelLocs) {
        var assembler = new BlockStateData();
        if (block.isFacingType()) {
            for (Direction d : Direction.values()) {
                ArrayList<VariantAssembler> vars = new ArrayList<>();
                for (ResourceLocation rl : modelLocs) {
                    vars.add(VariantAssembler.fromFacing(rl, d));
                }
                assembler.variants.put("facing=" + d.getName(), vars);

            }
        } else if (block.isAxisType()) {
            for (Direction.Axis a : Direction.Axis.values()) {
                ArrayList<VariantAssembler> vars = new ArrayList<>();
                for (ResourceLocation rl : modelLocs) {
                    vars.add(VariantAssembler.fromAxis(rl, a));
                }
                assembler.variants.put("axis=" + a.getName(), vars);

            }
        } else {
            ArrayList<VariantAssembler> vars = new ArrayList<>();
            for (ResourceLocation rl : modelLocs) {
                vars.add(VariantAssembler.fromModel(rl));
            }
            assembler.variants.put("", vars);
        }
        return assembler;
    }
}
