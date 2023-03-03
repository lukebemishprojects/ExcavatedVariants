package dev.lukebemish.excavatedvariants.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class BaseOre implements Cloneable {
    public static final Codec<BaseOre> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(bs -> bs.id),
            Codec.either(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("orename").forGetter(bs -> {
                if (bs.oreName.size() == 1 && bs.oreName.get(0).equals(bs.id)) return Optional.empty();
                return Optional.of(Either.right(bs.oreName));
            }),
            Codec.STRING.listOf().fieldOf("stone").forGetter(bs -> bs.stone),
            ResourceLocation.CODEC.listOf().fieldOf("block_id").forGetter(bs -> bs.blockId),
            Codec.STRING.optionalFieldOf("en_name").forGetter(bs -> Optional.empty()),
            Codec.STRING.listOf().fieldOf("types").forGetter(bs -> bs.types),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("lang", Map.of()).forGetter(bs -> bs.lang)
    ).apply(instance, BaseOre::new));

    public String id;
    public List<String> oreName;
    public List<String> stone;
    public List<ResourceLocation> blockId;
    public Map<String, String> lang;
    public List<String> types;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BaseOre(String id, Optional<Either<String, List<String>>> oreName, List<String> stone, List<ResourceLocation> blockId, Optional<String> enName, List<String> types, Map<String, String> lang) {
        this.id = id;
        this.oreName = new ArrayList<>();
        if (oreName.isPresent()) {
            var either = oreName.get();
            if (either.left().isPresent()) {
                this.oreName.add(either.left().get());
            } else if (either.right().isPresent()) {
                this.oreName.addAll(either.right().get());
            } else {
                this.oreName.add(id);
            }
        } else {
            this.oreName.add(id);
        }
        this.stone = stone;
        this.blockId = blockId;
        this.lang = new HashMap<>(lang);
        if (enName.isPresent() && !this.lang.containsKey("en_us")) {
            this.lang.put("en_us", enName.get());
        }
        this.types = types;
    }

    public BaseOre clone() {
        try {
            return (BaseOre) super.clone();
        } catch (CloneNotSupportedException e) {
            // Why would this happen?
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseOre baseStone = (BaseOre) o;
        return id.equals(baseStone.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
