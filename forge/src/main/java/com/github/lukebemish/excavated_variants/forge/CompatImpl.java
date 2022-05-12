package com.github.lukebemish.excavated_variants.forge;

import com.github.lukebemish.excavated_variants.api.ExcavatedVariantsListener;
import com.github.lukebemish.excavated_variants.api.IOreListModifier;

import java.util.List;

public class CompatImpl {
    public static List<IOreListModifier> getOreListModifiers() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, IOreListModifier.class);
    }
}
