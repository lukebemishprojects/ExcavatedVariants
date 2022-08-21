package io.github.lukebemish.excavated_variants;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LangBuilder {
    private static final Codec<Map<String,String>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);
    private final Map<String, String> internal = new HashMap<>();

    public void add(String fullId, BaseStone stone, BaseOre ore) {
        String enName = ore.en_name.contains("$") ? ore.en_name.replaceFirst("\\$", stone.en_name) : stone.en_name + " " + ore.en_name;
        internal.put("block."+ExcavatedVariants.MOD_ID+"."+fullId,enName);
    }

    public void add(String key, String name) {
        internal.put(key,name);
    }

    public Supplier<InputStream> build() {
        String json = ExcavatedVariants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal).getOrThrow(false, e->{}));
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
