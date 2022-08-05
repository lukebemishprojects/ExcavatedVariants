package io.github.lukebemish.excavated_variants.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SixSideBlockModelDefinition {
    Map<String, String> sides = new HashMap<>();
    Map<String, List<String>> overlays = new HashMap<>();


    public static SixSideBlockModelDefinition of(ResourceLocation base) {
        SixSideBlockModelDefinition out = new SixSideBlockModelDefinition();


        return out;
    }
}
