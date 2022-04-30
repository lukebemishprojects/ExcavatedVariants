package com.github.lukebemish.excavated_variants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class JsonHelper {
    public static Supplier<InputStream> getItemModel(String id) {
        String json = "{\"parent\": \""+ExcavatedVariants.MOD_ID+":"+"block/"+id+"0\"}";
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
