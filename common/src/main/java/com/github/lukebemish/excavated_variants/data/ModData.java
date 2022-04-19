package com.github.lukebemish.excavated_variants.data;

import com.google.gson.annotations.Expose;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ModData {
    public static final Codec<ModData> CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.STRING.fieldOf("mod_id").forGetter(x->x.mod_id),
            BaseStone.CODEC.listOf().fieldOf("provided_stones").forGetter(x->x.provided_stones),
            BaseOre.CODEC.listOf().fieldOf("provided_ores").forGetter(x->x.provided_ores)
    ).apply(instance,ModData::new));

    public String mod_id;
    public List<BaseStone> provided_stones;
    public List<BaseOre> provided_ores;

    public ModData(String id, List<BaseStone> provided_stones, List<BaseOre> provided_ores) {
        this.mod_id = id;
        this.provided_stones = provided_stones;
        this.provided_ores = provided_ores;
    }
}
