package io.github.lukebemish.excavated_variants.data;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.codecs.JanksonOps;
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
    public static final Path FULL_PATH = Services.PLATFORM.getModDataFolder().resolve("mappings_cache.json5");

    public static final Codec<MappingsCache> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf()).fieldOf("ore_mappings")
                    .forGetter(mc -> {
                        HashMap<String, List<ResourceLocation>> out = new HashMap<>();
                        mc.oreMappings.forEach((key, val) -> out.put(key, val.stream().toList()));
                        return out;
                    }),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("stone_mappings").forGetter(mc -> mc.stoneMappings)
    ).apply(instance, MappingsCache::new));

    public Map<String, Set<ResourceLocation>> oreMappings;
    public Map<String, ResourceLocation> stoneMappings;

    private static final JsonGrammar GRAMMAR = JsonGrammar.builder()
            .withComments(true)
            .printTrailingCommas(true)
            .bareSpecialNumerics(true)
            .printUnquotedKeys(true)
            .build();

    private MappingsCache(Map<String, List<ResourceLocation>> ores, Map<String, ResourceLocation> stones) {
        oreMappings = new HashMap<>();
        stoneMappings = new HashMap<>();
        ores.forEach((p, rls) -> oreMappings.put(p, Set.copyOf(rls)));
        stoneMappings.putAll(stones);
    }

    public static MappingsCache load() {
        try {
            if (!Files.exists(FULL_PATH)) throw new FileNotFoundException();
            JsonObject json = ModConfig.JANKSON.load(Files.newInputStream(FULL_PATH));
            return CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
            });
        } catch (FileNotFoundException e) {
            return new MappingsCache(new HashMap<>(), new HashMap<>());
        } catch (Exception e) {
            ExcavatedVariants.LOGGER.error("Issue loading mappings cache. Try deleting mod_data/excavated_variants/mappings_cache.json5. ", e);
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            if (!Files.exists(FULL_PATH.getParent())) Files.createDirectories(FULL_PATH.getParent());
            if (Files.exists(FULL_PATH)) Files.delete(FULL_PATH);
            var writer = Files.newBufferedWriter(FULL_PATH, StandardOpenOption.CREATE_NEW);
            JsonElement json = CODEC.encodeStart(JanksonOps.INSTANCE, this).getOrThrow(false, e -> {
            });
            json.toJson(writer, GRAMMAR, 0);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            ExcavatedVariants.LOGGER.error("Issue saving mappings cache. Something has gone very wrong. ", e);
            throw new RuntimeException(e);
        }
    }
}