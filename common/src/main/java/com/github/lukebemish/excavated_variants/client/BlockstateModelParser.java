package com.github.lukebemish.excavated_variants.client;

import com.github.lukebemish.excavated_variants.ModifiedOreBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockstateModelParser {
    public Map<String, List<VariantAssembler>> variants = new HashMap<>();

    public static BlockstateModelParser create(ModifiedOreBlock block, List<ResourceLocation> modelLocs) {
        var assembler = new BlockstateModelParser();
        if (block.isFacing()) {
            for (Direction d : Direction.values()) {
                ArrayList<VariantAssembler> vars = new ArrayList<>();
                for (ResourceLocation rl : modelLocs) {
                    vars.add(VariantAssembler.fromFacing(rl,d));
                }
                assembler.variants.put("facing="+d.getName(),vars);

            }
        } else if (block.isAxis()) {
            for (Direction.Axis a : Direction.Axis.values()) {
                ArrayList<VariantAssembler> vars = new ArrayList<>();
                for (ResourceLocation rl : modelLocs) {
                    vars.add(VariantAssembler.fromAxis(rl,a));
                }
                assembler.variants.put("axis="+a.getName(),vars);

            }
        } else {ArrayList<VariantAssembler> vars = new ArrayList<>();
            for (ResourceLocation rl : modelLocs) {
                vars.add(VariantAssembler.fromModel(rl));
            }
            assembler.variants.put("",vars);
        }
        return assembler;
    }
}
