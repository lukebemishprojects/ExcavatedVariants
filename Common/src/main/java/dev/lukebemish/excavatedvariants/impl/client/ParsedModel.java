/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSource;
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.*;
import dev.lukebemish.excavatedvariants.api.client.Face;
import dev.lukebemish.excavatedvariants.api.client.ModelData;
import dev.lukebemish.excavatedvariants.api.client.TexFaceProvider;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.codecs.JanksonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

// ONLY fit to be used for parsing, not for writing
public final class ParsedModel {

    public static @Nullable ResourceLocation resolveTexture(Map<String, String> map, String texName) {
        texName = texName.substring(1);
        String found = map.get(texName);
        if (found == null) return null;
        if (found.startsWith("#")) return resolveTexture(map, found);
        return ResourceLocation.of(found, ':');
    }

    public static String resolveTextureSymbol(Map<String, String> map, String texName) {
        texName = texName.substring(1);
        String found = map.get(texName);
        if (found == null) return texName;
        if (found.startsWith("#")) {
            return resolveTextureSymbol(map, found);
        }
        return texName;
    }

    public static final Codec<ParsedModel> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(ParsedModel::parent),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("textures", Map.of()).forGetter(ParsedModel::textures),
            ElementDefinition.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(ParsedModel::elements),
            Codec.unboundedMap(Codec.STRING, ParsedModel.CODEC).optionalFieldOf("children").forGetter(ParsedModel::children)
    ).apply(i, ParsedModel::new)));
    private final Optional<ResourceLocation> parent;
    private final Map<String, String> textures;
    private final List<ElementDefinition> elements;
    private final Optional<Map<String, ParsedModel>> children;

    public ParsedModel(Optional<ResourceLocation> parent, Map<String, String> textures,
                       List<ElementDefinition> elements, Optional<Map<String, ParsedModel>> children) {
        this.parent = parent;
        this.textures = textures;
        this.elements = elements;
        this.children = children;
    }

    private ResourceGenerationContext context;
    private Consumer<String> cacheKeyBuilder;

    @NotNull
    public static ParsedModel getFromLocation(ResourceLocation rl, ResourceGenerationContext context, Consumer<String> cacheKeyBuilder) throws IOException {
        try (InputStream is = BackupFetcher.getModelFile(rl, context, cacheKeyBuilder)) {
            JsonObject json = ExcavatedVariants.JANKSON.load(is);
            ParsedModel model = ParsedModel.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, e -> {
            });
            model.cacheKeyBuilder = cacheKeyBuilder;
            model.context = context;
            return model;
        } catch (SyntaxError | IOException | RuntimeException e) {
            throw new IOException("Could not read model " + rl, e);
        }
    }

    public Map<String, String> getTextureMap() throws IOException {
        Map<String, String> textures = new HashMap<>();
        ParsedModel parent = parent().isEmpty() ? null : getFromLocation(parent().get(), context, cacheKeyBuilder);
        if (parent != null)
            textures.putAll(parent.getTextureMap());
        textures.putAll(this.textures());
        return textures;
    }

    private Map<LocationKey, NamedResourceList> getRlMapForSide(String side) throws IOException {
        return getRlMapForSide(side, Map.of());
    }

    private Map<LocationKey, NamedResourceList> getRlMapForSide(String side, Map<String, String> oldTexMap) throws IOException {
        Map<LocationKey, NamedResourceList> map = new HashMap<>();

        var texMap = new HashMap<>(oldTexMap);
        texMap.putAll(getTextureMap());

        if (parent().isPresent()) {
            map.putAll(getFromLocation(parent().get(), context, cacheKeyBuilder).getRlMapForSide(side, texMap));
        }

        if (children().isEmpty() || children().get().isEmpty()) {
            for (ElementDefinition definition : this.elements()) {
                if (definition.faces.containsKey(side)) {
                    String texName = resolveTextureSymbol(texMap, definition.faces.get(side).texture());
                    LocationKey key = definition.getLocationKey();
                    NamedResourceList rls = map.computeIfAbsent(key, k -> new NamedResourceList(texName));
                    rls.name = texName;
                    ResourceLocation location = resolveTexture(texMap, "#" + texName);
                    if (location != null)
                        rls.resources.add(location);
                }
            }
        } else {
            for (var h : children().get().values()) {
                var m = h.getRlMapForSide(side, texMap);
                m.forEach((key, rls) -> map.merge(key, rls, (l1, l2) -> {
                    NamedResourceList out = new NamedResourceList(l1.name);
                    out.resources.addAll(l1.resources);
                    out.resources.addAll(l2.resources);
                    return out;
                }));
            }
        }
        return map;
    }

    public record SideInformation(Set<Face> faces, List<ResourceLocation> textureStack) {
    }

    private Map<String, SideInformation> processIntoSides() throws IOException {
        Map<String, SideInformation> sides = new HashMap<>();
        for (Face face : Face.values()) {
            var map = getRlMapForSide(face.faceName);
            for (NamedResourceList value : map.values()) {
                sides.computeIfAbsent(value.name, k -> new SideInformation(new HashSet<>(), new ArrayList<>(value.resources)))
                        .faces.add(face);
            }
        }
        return sides;
    }

    public ModelData makeStoneModel() throws IOException {
        Map<String, SideInformation> sides = processIntoSides();
        return new StoneModelData(this, sides);
    }

    public TexFaceProvider makeTextureProvider() throws IOException {
        Map<Face, List<ResourceLocation>> map = new HashMap<>();
        for (Face face : Face.values()) {
            var rlMap = getRlMapForSide(face.faceName);
            map.put(face, rlMap.values().stream().findFirst().map(it -> it.resources).orElse(List.of()));
        }
        return face -> (newStone, oldStone) -> {
            List<ResourceLocation> oreTextures = map.get(face);
            int[] c = new int[]{0};
            Map<String, TexSource> sourceMap = new HashMap<>();

            TexSource newStoneSource = newStone.apply(source -> {
                String name = "stoneNew" + c[0];
                c[0] += 1;
                sourceMap.put(name, source);
                return new AnimationFrameCapture.Builder().setCapture(name).build();
            });
            c[0] = 0;

            TexSource oldStoneSource = oldStone.apply(source -> {
                String name = "stoneOld" + c[0];
                c[0] += 1;
                sourceMap.put(name, source);
                return new AnimationFrameCapture.Builder().setCapture(name).build();
            });
            c[0] = 0;

            List<TexSource> oreSources = new ArrayList<>();
            for (ResourceLocation location : map.get(face)) {
                String name = "ore" + c[0];
                c[0] += 1;
                sourceMap.put(name, new TextureReaderSource.Builder().setPath(location).build());
                oreSources.add(new AnimationFrameCapture.Builder().setCapture(name).build());
            }

            return new Pair<>(new AnimationSplittingSource.Builder()
                    .setGenerator(new ForegroundTransferSource.Builder()
                            .setBackground(oldStoneSource)
                            .setFull(new OverlaySource.Builder().setSources(oreSources).build())
                            .setNewBackground(newStoneSource)
                            .build())
                    .setSources(sourceMap).build(), oreTextures);
        };
    }

    public Optional<ResourceLocation> parent() {
        return parent;
    }

    public Map<String, String> textures() {
        return textures;
    }

    public List<ElementDefinition> elements() {
        return elements;
    }

    public Optional<Map<String, ParsedModel>> children() {
        return children;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParsedModel) obj;
        return Objects.equals(this.parent, that.parent) &&
                Objects.equals(this.textures, that.textures) &&
                Objects.equals(this.elements, that.elements) &&
                Objects.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, textures, elements, children);
    }

    @Override
    public String toString() {
        return "ParsedModel[" +
                "parent=" + parent + ", " +
                "textures=" + textures + ", " +
                "elements=" + elements + ", " +
                "children=" + children + ']';
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

    public static class NamedResourceList {
        public String name;
        public final List<ResourceLocation> resources;

        public NamedResourceList(String name) {
            this.name = name;
            this.resources = new ArrayList<>();
        }
    }

    public record FaceDefinition(String texture) {
        public static final Codec<FaceDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("texture").forGetter(FaceDefinition::texture)
        ).apply(i, FaceDefinition::new));
    }


}
