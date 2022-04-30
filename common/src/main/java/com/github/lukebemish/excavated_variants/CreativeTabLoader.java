package com.github.lukebemish.excavated_variants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.CreativeModeTab;

public class CreativeTabLoader {

    @ExpectPlatform
    public static CreativeModeTab getCreativeTab() {
        throw new AssertionError();
    }
}
