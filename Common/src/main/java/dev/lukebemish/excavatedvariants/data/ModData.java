package dev.lukebemish.excavatedvariants.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ModData {
    public static final Codec<ModData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(Codec.STRING, Codec.STRING.listOf()).fieldOf("mod_id").forGetter(md -> {
                if (md.modId.size() == 1) return Either.left(md.modId.get(0));
                return Either.right(md.modId);
            }),
            BaseStone.CODEC.listOf().optionalFieldOf("provided_stones", List.of()).forGetter(x -> x.providedStones),
            BaseOre.CODEC.listOf().optionalFieldOf("provided_ores", List.of()).forGetter(x -> x.providedOres)
    ).apply(instance, ModData::new));

    public List<String> modId;
    public List<BaseStone> providedStones;
    public List<BaseOre> providedOres;

    public ModData(Either<String, List<String>> id, List<BaseStone> providedStones, List<BaseOre> providedOres) {
        if (id.left().isPresent()) this.modId = List.of(id.left().get());
        else this.modId = id.right().get();
        this.providedStones = providedStones;
        this.providedOres = providedOres;
    }
}
