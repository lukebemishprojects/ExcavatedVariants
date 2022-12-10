package dev.lukebemish.excavatedvariants;

import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JsonHelper {
    public static IoSupplier<InputStream> getItemModel(String id) {
        String json = "{\"parent\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "0\"}";
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
