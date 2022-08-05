package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class LangBuilder {
    private String internal = "";

    public void add(String fullId, BaseStone stone, BaseOre ore) {
        if (internal.length() >= 1) {
            internal += ",";
        }
        String enName = ore.en_name.contains("$") ? ore.en_name.replaceFirst("\\$", stone.en_name) : stone.en_name + " " + ore.en_name;
        internal += "\"block." + ExcavatedVariants.MOD_ID + "." + fullId + "\":\"" + enName + "\"";
    }

    public Supplier<InputStream> build() {
        String json = "{" + internal + "}";
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
