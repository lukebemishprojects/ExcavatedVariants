package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.api.IOreListModifier;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;

public class Compat {
    @ExpectPlatform
    public static List<IOreListModifier> getOreListModifiers() {
        throw new AssertionError();
    }
}
