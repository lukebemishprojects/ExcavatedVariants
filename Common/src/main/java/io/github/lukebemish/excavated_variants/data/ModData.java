package io.github.lukebemish.excavated_variants.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ModData {
    public static final Codec<ModData> CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.either(Codec.STRING,Codec.STRING.listOf()).fieldOf("mod_id").forGetter(md->{
                if (md.mod_id.size()==1) return Either.left(md.mod_id.get(0));
                return Either.right(md.mod_id);
            }),
            BaseStone.CODEC.listOf().fieldOf("provided_stones").forGetter(x->x.provided_stones),
            BaseOre.CODEC.listOf().fieldOf("provided_ores").forGetter(x->x.provided_ores)
    ).apply(instance,ModData::new));

    public List<String> mod_id;
    public List<BaseStone> provided_stones;
    public List<BaseOre> provided_ores;

    public ModData(Either<String,List<String>> id, List<BaseStone> provided_stones, List<BaseOre> provided_ores) {
        if (id.left().isPresent()) this.mod_id = List.of(id.left().get());
        else this.mod_id = id.right().get();
        this.provided_stones = provided_stones;
        this.provided_ores = provided_ores;
    }
}
