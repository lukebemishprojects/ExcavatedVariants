package dev.lukebemish.excavatedvariants.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.excavatedvariants.impl.data.BaseOre;
import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LangBuilder {
    private static final Codec<Map<String,String>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);
    private final Map<String, Map<String, String>> internal = new HashMap<>();

    public void add(String fullId, BaseStone stone, BaseOre ore) {
        Set<String> combinedKeys = new HashSet<>(stone.lang.keySet());
        combinedKeys.addAll(ore.lang.keySet());
        for (String langName : combinedKeys) {
            String stoneLang = stone.lang.getOrDefault(langName,"excavated_variants.stone." + stone.id);
            String oreLang = ore.lang.getOrDefault(langName,"excavated_variants.ore." + ore.id);
            String name = oreLang.contains("$") ? oreLang.replaceFirst("\\$", stoneLang) : stoneLang + " " + oreLang;
            internal.computeIfAbsent(langName, k -> new HashMap<>()).put("block."+ExcavatedVariants.MOD_ID+"."+fullId,name);
        }
    }

    public void add(String key, String name) {
        internal.computeIfAbsent("en_us", k -> new HashMap<>()).put(key,name);
    }

    public Set<String> languages() {
        return internal.keySet();
    }

    public IoSupplier<InputStream> build(String langName) {
        String json = ExcavatedVariants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal.getOrDefault(langName, Map.of())).getOrThrow(false, e->{}));
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
