/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
import dev.lukebemish.excavatedvariants.impl.data.ModIDBlockStoneMapping;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.*;

public final class Ore {
    public static final Codec<Ore> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.listOf().fieldOf("tags").forGetter(o -> o.tags),
            ModIDBlockStoneMapping.MAP_CODEC.fieldOf("blocks").forGetter(o -> o.blocks),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translations").forGetter(o -> o.translations),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types)
    ).apply(i, Ore::new));

    public final List<ResourceLocation> tags;
    private final Map<ResourceLocation, ResourceKey<Stone>> blocks;
    private Map<ResourceKey<Block>, ResourceKey<Stone>> blocksBaked;
    private final Map<ResourceLocation, ResourceKey<Stone>> generatedBlocks = new HashMap<>();
    private final Map<ResourceKey<Stone>, ResourceKey<Block>> originalStoneBlocks;
    private Map<ResourceKey<Stone>, ResourceKey<Block>> generatedStoneBlocks;
    public final Map<String, String> translations;
    public final Set<ResourceKey<GroundType>> types;
    private boolean baked = false;

    private Ore(List<ResourceLocation> tags, Map<ResourceLocation, ResourceKey<Stone>> blocks, Map<String, String> translations, Set<ResourceKey<GroundType>> types) {
        this.tags = tags;
        this.blocks = new HashMap<>(blocks);
        this.translations = translations;
        this.types = types;

        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new HashMap<>();
        Map<ResourceKey<Stone>, ResourceKey<Block>> original = new HashMap<>();
        for (Map.Entry<ResourceLocation, ResourceKey<Stone>> entry : blocks.entrySet()) {
            ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
            baked.put(block, entry.getValue());
            original.put(entry.getValue(), block);
        }
        this.blocksBaked = Collections.unmodifiableMap(baked);
        this.originalStoneBlocks = Collections.unmodifiableMap(original);
        this.generatedStoneBlocks = Collections.unmodifiableMap(new HashMap<>());
    }

    public Map<ResourceKey<Block>, ResourceKey<Stone>> getBlocks() {
        return blocksBaked;
    }

    @ApiStatus.Internal
    public void addPossibleVariant(Stone stone, ResourceLocation output) {
        blocks.put(output, stone.getKeyOrThrow());
    }

    @ApiStatus.Internal
    public synchronized void bakeExistingBlocks() {
        if (baked) return;
        baked = true;
        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new HashMap<>();
        for (var entry : blocks.entrySet()) {
            if (BuiltInRegistries.BLOCK.containsKey(entry.getKey())) {
                ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
                baked.put(block, entry.getValue());
            }
        }
        Map<ResourceKey<Stone>, ResourceKey<Block>> bakedGenerated = new HashMap<>();
        for (var entry : generatedBlocks.entrySet()) {
            if (BuiltInRegistries.BLOCK.containsKey(entry.getKey())) {
                ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
                bakedGenerated.put(entry.getValue(), block);
            }
        }
        blocksBaked = Collections.unmodifiableMap(baked);
        generatedStoneBlocks = Collections.unmodifiableMap(bakedGenerated);
        this.blocks.clear();
        this.generatedBlocks.clear();
    }

    @Contract(value = "_ -> new", pure = true)
    public static Ore merge(List<Ore> ores) {
        List<ResourceLocation> tags = new ArrayList<>();
        Set<ResourceLocation> tagsSet = new HashSet<>();
        Map<ResourceLocation, ResourceKey<Stone>> blocks = new HashMap<>();
        Map<String, String> translation = new HashMap<>();
        Set<ResourceKey<GroundType>> types = new HashSet<>();
        for (Ore ore : ores) {
            for (ResourceLocation alternativeName : ore.tags) {
                if (!tagsSet.contains(alternativeName)) {
                    tagsSet.add(alternativeName);
                    tags.add(alternativeName);
                }
            }
            blocks.putAll(ore.blocks);
            translation.putAll(ore.translations);
            types.addAll(ore.types);
        }
        return new Ore(tags, blocks, translation, types);
    }

    public Holder<Ore> getHolder() {
        return RegistriesImpl.ORE_REGISTRY.wrapAsHolder(this);
    }

    public ResourceKey<Ore> getKeyOrThrow() {
        return getHolder().unwrapKey().orElseThrow(() -> new IllegalStateException("Unregistered ore"));
    }

    public Map<ResourceKey<Stone>, ResourceKey<Block>> getOriginalStoneBlocks() {
        return originalStoneBlocks;
    }

    public Map<ResourceKey<Stone>, ResourceKey<Block>> getGeneratedStoneBlocks() {
        return generatedStoneBlocks;
    }

    public static class Builder {
        private List<ResourceLocation> tags;
        private Map<ResourceLocation, ResourceKey<Stone>> blocks;
        private Map<String, String> translations;
        private Set<ResourceKey<GroundType>> types;

        public Builder setTags(List<ResourceLocation> tags) {
            this.tags = tags;
            return this;
        }

        public Builder setBlocks(Map<ResourceLocation, ResourceKey<Stone>> blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder setTranslations(Map<String, String> translations) {
            this.translations = translations;
            return this;
        }

        public Builder setTypes(Set<ResourceKey<GroundType>> types) {
            this.types = types;
            return this;
        }

        public Ore build() {
            Objects.requireNonNull(tags);
            Objects.requireNonNull(blocks);
            Objects.requireNonNull(translations);
            Objects.requireNonNull(types);
            return new Ore(tags, blocks, translations, types);
        }
    }

    public TagKey<Block> getTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores/"));
    }

    public TagKey<Block> getConvertibleTagKey() {
        return TagKey.create(Registries.BLOCK, getKeyOrThrow().location().withPrefix("ores_convertible/"));
    }
}
