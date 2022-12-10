package dev.lukebemish.excavatedvariants.client;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.codecs.JanksonOps;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

// ONLY fit to be used for parsing, not for writing
public record BlockModelHolder(Optional<ResourceLocation> parent, Map<String, String> textures,
                               List<ElementDefinition> elements, Optional<Map<String, BlockModelHolder>> children) {
    public static final List<String> SIDES = List.of("down", "up", "north", "south", "west", "east");
    private static final Jankson JANKSON = Jankson.builder().build();

    public static @Nullable ResourceLocation resolveTexture(Map<String, String> map, String texName) {
        texName = texName.substring(1);
        String found = map.get(texName);
        if (found == null) return null;
        if (found.startsWith("#")) return resolveTexture(map, found);
        return ResourceLocation.of(found, ':');
    }    public static final Codec<BlockModelHolder> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(BlockModelHolder::parent),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("textures", Map.of()).forGetter(BlockModelHolder::textures),
            ElementDefinition.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(BlockModelHolder::elements),
            Codec.unboundedMap(Codec.STRING, BlockModelHolder.CODEC).optionalFieldOf("children").forGetter(BlockModelHolder::children)
    ).apply(i, BlockModelHolder::new)));

    @Nullable
    public static BlockModelHolder getFromLocation(ResourceLocation rl) {
        try (InputStream is = BackupFetcher.tryAndProvideModelFile(rl)) {
            JsonObject json = JANKSON.load(is);
            return BlockModelHolder.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
            });
        } catch (SyntaxError | IOException | RuntimeException e) {
            ExcavatedVariants.LOGGER.error("Could not read model {}", rl, e);
        }
        return null;
    }

    public static BlockModelHolder getFromString(String string) {
        try {
            JsonObject json = JANKSON.load(string);
            return BlockModelHolder.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
            });
        } catch (SyntaxError | RuntimeException e) {
            ExcavatedVariants.LOGGER.error("Could not read model:\n{}", string, e);
        }
        return null;
    }

    private static ResourceLocation resolveTexture(Map<String, String> map, Set<String> stack, String name) {
        String out = map.get(name);
        if (out == null)
            return null;
        try {
            return ResourceLocation.of(out, ':');
        } catch (ResourceLocationException ignored) {
        }
        out = out.substring(1);
        if (stack.contains(out))
            return null;
        stack.add(out);
        return resolveTexture(map, stack, out);
    }

    public Map<String, String> getTextureMap() {
        Map<String, String> textures = new HashMap<>();
        BlockModelHolder parent = parent().isEmpty() ? null : getFromLocation(parent().get());
        if (parent != null)
            textures.putAll(parent.getTextureMap());
        textures.putAll(this.textures());
        return textures;
    }

    public Map<String, ResourceLocation> getResolvedTextureMap() {
        Map<String, ResourceLocation> out = new HashMap<>();
        Map<String, String> textures = getTextureMap();
        textures.keySet().forEach(k -> {
            ResourceLocation rl = resolveTexture(textures, new HashSet<>(), k);
            if (rl != null)
                out.put(k, rl);
        });
        return out;
    }

    private Map<LocationKey, List<ResourceLocation>> getRlMapForSide(String side) {
        return getRlMapForSide(side, Map.of());
    }

    private Map<LocationKey, List<ResourceLocation>> getRlMapForSide(String side, Map<String, String> oldTexMap) {
        Map<LocationKey, List<ResourceLocation>> map = new HashMap<>();

        var texMap = new HashMap<>(oldTexMap);
        texMap.putAll(getTextureMap());

        if (parent().isPresent()) {
            map.putAll(getFromLocation(parent().get()).getRlMapForSide(side, texMap));
        }

        if (children().isEmpty() || children().get().isEmpty()) {
            for (ElementDefinition definition : this.elements()) {
                if (definition.faces.containsKey(side)) {
                    LocationKey key = definition.getLocationKey();
                    List<ResourceLocation> rls = map.computeIfAbsent(key, k -> new ArrayList<>());
                    FaceDefinition face = definition.faces.get(side);
                    String texture = face.texture();
                    ResourceLocation location = resolveTexture(texMap, texture);
                    if (location != null)
                        rls.add(location);
                }
            }
        } else {
            List<Map<LocationKey, List<ResourceLocation>>> maps = children().get().values().stream().map(h -> h.getRlMapForSide(side, texMap)).toList();
            maps.forEach(m -> m.forEach((key, rls) -> map.merge(key, rls, (l1, l2) -> {
                List<ResourceLocation> rlList = new ArrayList<>(l1);
                rlList.addAll(l2);
                return rlList;
            })));
        }
        return map;
    }

    public List<List<ResourceLocation>> setupOverlays() {
        final Set<List<ResourceLocation>> overlays = new HashSet<>();
        for (String side : SIDES) {
            var sideMap = this.getRlMapForSide(side);
            overlays.addAll(sideMap.values().stream().filter(l -> !l.isEmpty()).toList());
        }

        return overlays.stream().toList();
    }

    public record ElementDefinition(Map<String, FaceDefinition> faces, List<Integer> from, List<Integer> to,
                                    RotationDefinition rotation) {
        public static final Codec<ElementDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.unboundedMap(Codec.STRING, FaceDefinition.CODEC).optionalFieldOf("faces", Map.of()).forGetter(ElementDefinition::faces),
                Codec.INT.listOf().optionalFieldOf("from", List.of(0, 0, 0)).forGetter(ElementDefinition::from),
                Codec.INT.listOf().optionalFieldOf("to", List.of(16, 16, 16)).forGetter(ElementDefinition::to),
                RotationDefinition.CODEC.optionalFieldOf("rotation", new RotationDefinition(List.of(0, 0, 0), "x", 0)).forGetter(ElementDefinition::rotation)
        ).apply(i, ElementDefinition::new));

        public LocationKey getLocationKey() {
            List<Integer> from = Stream.of(0, 1, 2).map(i -> Math.min(from().get(i), to().get(i))).toList();
            List<Integer> to = Stream.of(0, 1, 2).map(i -> Math.max(from().get(i), to().get(i))).toList();
            return new LocationKey(rotation().origin(), rotation().axis(), rotation().angle(),
                    from, to);
        }
    }

    public record RotationDefinition(List<Integer> origin, String axis, float angle) {
        public static final Codec<RotationDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.listOf().fieldOf("origin").forGetter(RotationDefinition::origin),
                Codec.STRING.fieldOf("axis").forGetter(RotationDefinition::axis),
                Codec.FLOAT.fieldOf("angle").forGetter(RotationDefinition::angle)
        ).apply(i, RotationDefinition::new));
    }

    public record LocationKey(List<Integer> origin, String axis, float angle, List<Integer> of, List<Integer> to) {

    }

    public record FaceDefinition(String texture) {
        public static final Codec<FaceDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("texture").forGetter(FaceDefinition::texture)
        ).apply(i, FaceDefinition::new));
    }


}
