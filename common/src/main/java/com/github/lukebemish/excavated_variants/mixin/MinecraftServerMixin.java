package com.github.lukebemish.excavated_variants.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerMixin {
    @Accessor
    RegistryAccess.Frozen getRegistryHolder();
}
