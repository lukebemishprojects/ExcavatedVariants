package io.github.lukebemish.excavated_variants.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MappingsCache {
    public static final Path FULL_PATH = Services.PLATFORM.getModDataFolder().resolve("mappings_cache.json");

    public static final Codec<MappingsCache> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf()).fieldOf("ore_mappings")
                    .forGetter(mc -> {
                        HashMap<String, List<ResourceLocation>> out = new HashMap<>();
                        mc.oreMappings.forEach((key,val)->out.put(key,val.stream().toList()));
                        return out;
                    }),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("stone_mappings").forGetter(mc -> mc.stoneMappings)
    ).apply(instance, MappingsCache::new));

    public Map<String, Set<ResourceLocation>> oreMappings;
    public Map<String, ResourceLocation> stoneMappings;

    private MappingsCache(Map<String, List<ResourceLocation>> ores, Map<String, ResourceLocation> stones) {
        oreMappings = new HashMap<>();
        stoneMappings = new HashMap<>();
        ores.forEach((p,rls) -> oreMappings.put(p, Set.copyOf(rls)));
        stoneMappings.putAll(stones);
    }

    public static MappingsCache load() {
        try {
            if (!Files.exists(FULL_PATH)) throw new FileNotFoundException();
            JsonObject json = ModConfig.GSON.fromJson(Files.newBufferedReader(FULL_PATH), JsonObject.class);
            return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, e -> {});
        } catch (FileNotFoundException e) {
            return new MappingsCache(new HashMap<>(), new HashMap<>());
        } catch (Exception e) {
            ExcavatedVariants.LOGGER.error("Issue loading mappings cache. Try deleting mod_data/excavated_variants/mappings_cache.json. ",e);
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            if (!Files.exists(FULL_PATH.getParent())) Files.createDirectories(FULL_PATH.getParent());
            if (Files.exists(FULL_PATH)) Files.delete(FULL_PATH);
            var writer = Files.newBufferedWriter(FULL_PATH, StandardOpenOption.CREATE_NEW);
            JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e -> {});
            ModConfig.GSON.toJson(json, writer);
            writer.flush();
            writer.close();
            System.out.println("Saved mappings cache...");
        } catch (Exception e) {
            ExcavatedVariants.LOGGER.error("Issue saving mappings cache. Something has gone very wrong. ",e);
            throw new RuntimeException(e);
        }
    }
}