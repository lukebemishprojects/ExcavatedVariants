package com.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class BaseOre implements Cloneable {
    public static final Codec<BaseOre> CODEC = RecordCodecBuilder.create((instance)-> instance.group(
            Codec.STRING.fieldOf("id").forGetter(bs->bs.id),
            Codec.STRING.optionalFieldOf("ore_name").forGetter(bs->Optional.of(bs.orename)),
            Codec.STRING.listOf().fieldOf("stone").forGetter(bs->bs.stone),
            ResourceLocation.CODEC.optionalFieldOf("texture_location").forGetter(bs->Optional.empty()),
            ResourceLocation.CODEC.listOf().fieldOf("block_id").forGetter(bs->bs.block_id),
            Codec.STRING.fieldOf("en_name").forGetter(bs-> bs.en_name),
            Codec.STRING.listOf().fieldOf("types").forGetter(bs->bs.types),
            Codec.INT.optionalFieldOf("texture_count",5).forGetter(bs->bs.texture_count)
            ).apply(instance,BaseOre::new));

    public String id;
    public String orename;
    public List<String> stone;
    public List<ResourceLocation> block_id;
    public String en_name;
    public List<String> types;
    public int texture_count = 5;

    public BaseOre(String id, Optional<String> orename, List<String> stone, Optional<ResourceLocation> texture_location, List<ResourceLocation> block_id, String en_name, List<String> types,int texture_count) {
        this.id = id;
        this.orename = orename.isEmpty()?id:orename.get();
        this.stone = stone;
        this.block_id = block_id;
        this.en_name = en_name;
        this.types = types;
        this.texture_count = texture_count;
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
