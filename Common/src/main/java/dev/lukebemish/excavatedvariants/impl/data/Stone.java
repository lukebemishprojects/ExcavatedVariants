package dev.lukebemish.excavatedvariants.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Stone {
    public static final Codec<Stone> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translation").forGetter(s -> s.translation),
            ResourceKey.codec(Registries.BLOCK).fieldOf("block").forGetter(s -> s.block),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types)
    ).apply(i, Stone::new));

    public final Map<String, String> translation;
    public final ResourceKey<Block> block;
    public final Set<ResourceKey<GroundType>> types;

    public Stone(Map<String, String> translation, ResourceKey<Block> block, Set<ResourceKey<GroundType>> types) {
        this.translation = translation;
        this.block = block;
        this.types = types;
    }

    public final Holder<Stone> getHolder() {
        return RegistriesImpl.STONE_REGISTRY.wrapAsHolder(this);
    }

    public final ResourceKey<Stone> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered stone"));
    }
}
