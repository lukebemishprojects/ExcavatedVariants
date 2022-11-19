package io.github.lukebemish.excavated_variants;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.dynamic_asset_generator.api.ServerPrePackRepository;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiningLevelTagHolder implements Supplier<Map<ResourceLocation, Set<ResourceLocation>>> {
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();
    public void add(String fullId, BaseOre ore, BaseStone stone) {
        toCheck.add(new CheckPair(fullId, ore.block_id.get(0), stone.block_id));
    }

    @Override
    public Map<ResourceLocation, Set<ResourceLocation>> get() {
        Map<ResourceLocation, Set<ResourceLocation>> tags = new HashMap<>();

        List<ResourceLocation> tagNames = Services.PLATFORM.getMiningLevels();
        System.out.println(tagNames);
        Map<ResourceLocation, Integer> blockToLevelMap = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> memberMap = tagNames.stream().collect(Collectors.toMap(Function.identity(), this::getTagMembers));

        toCheck.forEach(pair -> {
            int maxValue = Math.max(getOrCreateLevel(tagNames, memberMap, blockToLevelMap, pair.stoneId),
                    getOrCreateLevel(tagNames, memberMap, blockToLevelMap, pair.oreId));
            if (maxValue != -1)
                tags.computeIfAbsent(tagNames.get(maxValue), k->new HashSet<>()).add(new ResourceLocation(ExcavatedVariants.MOD_ID, pair.fullId));
        });

        return tags;
    }

    private record CheckPair(String fullId, ResourceLocation oreId, ResourceLocation stoneId) {
    }

    private int getOrCreateLevel(List<ResourceLocation> levels, Map<ResourceLocation, List<ResourceLocation>> memberMap, Map<ResourceLocation, Integer> map, ResourceLocation lookup) {
        return map.computeIfAbsent(lookup, key -> {
            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : memberMap.entrySet()) {
                if (entry.getValue().contains(key)) {
                    return levels.indexOf(entry.getKey());
                }
            }
            return -1;
        });
    }

    private List<ResourceLocation> getTagMembers(ResourceLocation location) {
        String type = location.getPath().split("/")[0];
        List<ResourceLocation> members = new ArrayList<>();
        var toRead = new ResourceLocation(location.getNamespace(), "tags/"+location.getPath()+".json");
        try (var read = ServerPrePackRepository.getResources(toRead)) {
            read.forEach(is -> {
                try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    var parser = JsonParser.parseReader(reader);
                    try {
                        TagFile file = TagFile.CODEC.parse(JsonOps.INSTANCE, parser).getOrThrow(false, e->{});
                        if (file.replace)
                            members.clear();
                        file.values.forEach(value -> {
                            value.left().ifPresent(members::add);
                            value.right().ifPresent(tag ->
                                    members.addAll(getTagMembers(new ResourceLocation(tag.getNamespace(), type+"/"+tag.getPath()))));
                        });
                    } catch (RuntimeException e) {
                        ExcavatedVariants.LOGGER.error("Issue parsing tag at '{}':",toRead,e);
                    }
                } catch (IOException e) {
                    ExcavatedVariants.LOGGER.error("Issue reading tag at '{}':",toRead,e);
                }
            });
        } catch (IOException ignored) {
            // Tag just doesn't exist
        }
        return members;
    }

    // Either is location, then tag
    private record TagFile(List<Either<ResourceLocation, ResourceLocation>> values, boolean replace) {
        public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(p-> p.group(
                Codec.either(ResourceLocation.CODEC, Codec.STRING.flatXmap(s->{
                    if (!s.startsWith("#"))
                        return DataResult.error("Tag must start with '#'");
                    var location = ResourceLocation.tryParse(s.substring(1));
                    if (location == null)
                        return DataResult.error("Invalid tag location");
                    return DataResult.success(location);
                },rl-> DataResult.success("#"+rl))).listOf().optionalFieldOf("values",List.of()).forGetter(TagFile::values),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(TagFile::replace)
        ).apply(p, TagFile::new));
    }
}
