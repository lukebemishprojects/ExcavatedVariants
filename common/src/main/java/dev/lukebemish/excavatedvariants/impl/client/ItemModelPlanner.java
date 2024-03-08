package dev.lukebemish.excavatedvariants.impl.client;

import com.google.gson.JsonObject;
import dev.lukebemish.dynamicassetgenerator.api.PathAwareInputStreamSource;
import dev.lukebemish.dynamicassetgenerator.api.Resettable;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemModelPlanner implements PathAwareInputStreamSource, Resettable {
    private final List<String> ids = new ArrayList<>();

    @Nullable
    private JsonObject baseModel;
    boolean tried = false;

    public void add(String id) {
        ids.add(id);
    }

    @Override
    public @NonNull Set<ResourceLocation> getLocations(ResourceGenerationContext context) {
        return ids.stream().map(fullId -> new ResourceLocation(ExcavatedVariants.MOD_ID, "models/item/" + fullId + ".json")).collect(Collectors.toSet());
    }

    @Override
    public @Nullable IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        try {
            if (baseModel == null) {
                setupBaseModel(context);
            }
        } catch (IOException e) {
            return null;
        }

        return () -> {
            var json = baseModel.deepCopy();
            json.addProperty("parent", ExcavatedVariants.MOD_ID + ":block/" + outRl.getPath().replace(".json", "").replace("models/item/", "") + "__0");
            return new ByteArrayInputStream(ExcavatedVariants.GSON.toJson(json).getBytes());
        };
    }

    private synchronized void setupBaseModel(ResourceGenerationContext context) throws IOException {
        var resource = context.getResourceSource().getResource(new ResourceLocation("models/block/block.json"));
        if (resource == null) {
            if (!tried) {
                tried = true;
                throw new IOException("Failed to load base block model; resource not found");
            }
            return;
        }
        try (var is = resource.get()) {
            baseModel = ExcavatedVariants.GSON.fromJson(new String(is.readAllBytes()), JsonObject.class);
        } catch (Exception e) {
            if (!tried) {
                tried = true;
                ExcavatedVariants.LOGGER.error("Failed to load base block model", e);
            }
            throw new IOException(e);
        }
    }

    @Override
    public void reset(ResourceGenerationContext context) {
        baseModel = null;
        tried = false;
    }
}
