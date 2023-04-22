package dev.lukebemish.excavatedvariants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.minecraft.server.packs.resources.IoSupplier;

public class JsonHelper {
    public static IoSupplier<InputStream> getItemModel(String id) {
        String json = "{\"parent\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "_0\"}";
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
