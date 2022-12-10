package dev.lukebemish.excavatedvariants;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    public IoSupplier<InputStream> build() {
        String json = ExcavatedVariants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal).getOrThrow(false, e->{}));
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
