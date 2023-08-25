package dev.lukebemish.excavatedvariants.impl.mixin;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface MappedRegistryMixin {
    @Accessor("frozen")
    void setFrozen(boolean frozen);
}
