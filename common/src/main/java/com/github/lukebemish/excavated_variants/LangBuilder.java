package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class LangBuilder {
    private String internal = "";

    public void add(String full_id, BaseStone stone, BaseOre ore) {
        if (internal.length() >= 1) {
            internal += ",";
        }
        internal += "\"block."+ExcavatedVariants.MOD_ID+"."+full_id+"\":\""+stone.en_name+" "+ore.en_name+"\"";
    }

    public Supplier<InputStream> build() {
        String json = "{"+internal+"}";
        return () -> {
            return new ByteArrayInputStream(json.getBytes());
        };
    }
}
