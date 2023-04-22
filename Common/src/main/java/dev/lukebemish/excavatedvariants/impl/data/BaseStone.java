package dev.lukebemish.excavatedvariants.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class BaseStone {
    public static final Codec<BaseStone> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(bs -> bs.id),
            Codec.STRING.optionalFieldOf("en_name").forGetter(bs -> Optional.empty()),
            ResourceLocation.CODEC.fieldOf("block_id").forGetter(bs -> bs.blockId),
            Codec.STRING.listOf().fieldOf("types").forGetter(bs -> bs.types),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("lang", Map.of()).forGetter(bs -> bs.lang)
    ).apply(instance, BaseStone::new));

    public String id;
    public Map<String, String> lang;
    public ResourceLocation blockId;
    public List<String> types;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BaseStone(String id, Optional<String> enName, ResourceLocation blockId, List<String> types, Map<String, String> lang) {
        this.id = id;
        this.blockId = blockId;
        this.types = types;
        this.lang = new HashMap<>(lang);
        if (enName.isPresent() && !this.lang.containsKey("en_us")) {
            this.lang.put("en_us", enName.get());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseStone baseStone = (BaseStone) o;
        return id.equals(baseStone.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
