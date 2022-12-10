package dev.lukebemish.excavatedvariants.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface IMinecraftServerMixin {
    @Accessor
    RegistryAccess.Frozen getRegistryHolder();
}
