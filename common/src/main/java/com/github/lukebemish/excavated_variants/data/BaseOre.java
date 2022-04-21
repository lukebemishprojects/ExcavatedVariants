package com.github.lukebemish.excavated_variants.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BaseOre implements Cloneable {
    public static final Codec<BaseOre> CODEC = RecordCodecBuilder.create((instance)-> instance.group(
            Codec.STRING.fieldOf("id").forGetter(bs->bs.id),
            Codec.either(Codec.STRING,Codec.STRING.listOf()).optionalFieldOf("orename").forGetter(bs->{
                if (bs.orename.size()==1 && bs.orename.get(0).equals(bs.id)) return Optional.empty();
                return Optional.of(Either.right(bs.orename));
            }),
            Codec.STRING.listOf().fieldOf("stone").forGetter(bs->bs.stone),
            ResourceLocation.CODEC.listOf().fieldOf("block_id").forGetter(bs->bs.block_id),
            Codec.STRING.fieldOf("en_name").forGetter(bs-> bs.en_name),
            Codec.STRING.listOf().fieldOf("types").forGetter(bs->bs.types),
            Codec.INT.optionalFieldOf("texture_count").forGetter(bs->{
                if (bs.orename.size()==5) return Optional.empty();
                return Optional.of(bs.texture_count);
            })
            ).apply(instance,BaseOre::new));

    public String id;
    public List<String> orename;
    public List<String> stone;
    public List<ResourceLocation> block_id;
    public String en_name;
    public List<String> types;
    public int texture_count;

    public BaseOre(String id, Optional<Either<String,List<String>>> orename, List<String> stone, List<ResourceLocation> block_id, String en_name, List<String> types,Optional<Integer> texture_count) {
        this.id = id;
        this.orename = new ArrayList<>();
        if (orename.isPresent()) {
            var either = orename.get();
            if (either.left().isPresent()) {
                this.orename.add(either.left().get());
            } else if (either.right().isPresent()) {
                this.orename.addAll(either.right().get());
            } else {
                this.orename.add(id);
            }
        } else {
            this.orename.add(id);
        }
        this.stone = stone;
        this.block_id = block_id;
        this.en_name = en_name;
        this.types = types;
        this.texture_count = texture_count.orElse(5);
    }
    public BaseOre clone() {
        try {
            return (BaseOre)super.clone();
        } catch (CloneNotSupportedException e) {
            // Why would this happen?
            return null;
        }
    }
}
