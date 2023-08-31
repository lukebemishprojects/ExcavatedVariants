/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.excavatedvariants.api.RegistryKeys;
import dev.lukebemish.excavatedvariants.api.data.modifier.Flag;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.ModLifecycle;
import dev.lukebemish.excavatedvariants.impl.RegistriesImpl;
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
            DetailedStone.MAP_CODEC.fieldOf("blocks").forGetter(o -> o.blocks),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translations").forGetter(o -> o.translations),
            ResourceKey.codec(RegistryKeys.GROUND_TYPE).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("types").forGetter(o -> o.types)
    ).apply(i, Ore::new));

    public final List<ResourceLocation> tags;
    private final Map<ResourceLocation, DetailedStone> blocks;
    private Map<ResourceKey<Block>, ResourceKey<Stone>> blocksBaked;
    public final Set<ResourceKey<Stone>> originalStones;
    public final Set<ResourceKey<Block>> originalBlocks;
    private final Set<ResourceKey<Stone>> originalStonesPrivate;
    private final Set<ResourceKey<Block>> originalBlocksPrivate;
    private Map<ResourceKey<Stone>, ResourceKey<Block>> blocksBakedInverse;
    private Map<ResourceKey<Block>, ResourceKey<Stone>> generatingBlocks;
    public final Map<String, String> translations;
    public final Set<ResourceKey<GroundType>> types;
    private boolean baked = false;

    private Ore(List<ResourceLocation> tags, Map<ResourceLocation, DetailedStone> blocks, Map<String, String> translations, Set<ResourceKey<GroundType>> types) {
        this.tags = tags;
        this.blocks = new HashMap<>(blocks);
        this.translations = translations;
        this.types = types;

        this.originalStonesPrivate = new HashSet<>();
        this.originalBlocksPrivate = new HashSet<>();
        this.originalStones = Collections.unmodifiableSet(this.originalStonesPrivate);
        this.originalBlocks = Collections.unmodifiableSet(this.originalBlocksPrivate);
        bakeOriginal();
    }

    private void bakeOriginal() {
        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new IdentityHashMap<>();
        Map<ResourceKey<Stone>, ResourceKey<Block>> bakedInverse = new IdentityHashMap<>();
        Map<ResourceKey<Block>, ResourceKey<Stone>> generatingBlocks = new IdentityHashMap<>();
        this.originalBlocksPrivate.clear();
        this.originalStonesPrivate.clear();
        var iterator = blocks.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().hasRequiredMods()) {
                ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
                baked.put(block, entry.getValue().getStone());
                bakedInverse.put(entry.getValue().getStone(), block);
                this.originalBlocksPrivate.add(block);
                this.originalStonesPrivate.add(entry.getValue().getStone());
                if (entry.getValue().isGenerating()) {
                    generatingBlocks.put(block, entry.getValue().getStone());
                }
            } else {
                iterator.remove();
            }
        }
        this.blocksBaked = Collections.unmodifiableMap(baked);
        this.blocksBakedInverse = Collections.unmodifiableMap(bakedInverse);
        this.generatingBlocks = Collections.unmodifiableMap(generatingBlocks);
    }

    public Map<ResourceKey<Block>, ResourceKey<Stone>> getBlocks() {
        return blocksBaked;
    }

    public Map<ResourceKey<Block>, ResourceKey<Stone>> getGeneratingBlocks() {
        return generatingBlocks;
    }

    public Map<ResourceKey<Stone>, ResourceKey<Block>> getStones() {
        return blocksBakedInverse;
    }

    @ApiStatus.Internal
    public void addPossibleVariant(Stone stone, ResourceLocation output) {
        if (ModLifecycle.getLifecyclePhase() != ModLifecycle.PRE_REGISTRATION) {
            throw new IllegalStateException("Cannot add possible variant except during pre-registration");
        }
        blocks.put(output, new DetailedStone.Builder().setModIds(List.of(ExcavatedVariants.MOD_ID)).setStone(stone.getKeyOrThrow()).build());
    }

    @ApiStatus.Internal
    public void modifyOriginal(Modifier modifier) {
        for (var entry : this.blocksBaked.entrySet()) {
            Stone stone = RegistriesImpl.STONE_REGISTRY.get(entry.getValue());
            if (stone == null) throw new IllegalStateException("Stone " + entry.getValue().location() + " is not registered but is referenced by ore " + getKeyOrThrow().location());
            if (modifier.variantFilter.matches(this, stone, entry.getKey().location())) {
                if (modifier.flags.contains(Flag.DISABLE)) {
                    blocks.remove(entry.getKey().location());
                } else if (modifier.flags.contains(Flag.NON_GENERATING)) {
                    blocks.put(entry.getKey().location(), DetailedStone.Builder.of(blocks.get(entry.getKey().location())).setGenerating(false).build());
                }
            }
        }
    }

    private void bakeVariants() {
        if (baked) return;
        baked = true;
        Map<ResourceKey<Block>, ResourceKey<Stone>> baked = new HashMap<>();
        Map<ResourceKey<Stone>, ResourceKey<Block>> bakedInverse = new IdentityHashMap<>();
        for (var entry : blocks.entrySet()) {
            if (BuiltInRegistries.BLOCK.containsKey(entry.getKey())) {
                ResourceKey<Block> block = ResourceKey.create(Registries.BLOCK, entry.getKey());
                baked.put(block, entry.getValue().getStone());
                bakedInverse.put(entry.getValue().getStone(), block);
            }
        }
        blocksBaked = Collections.unmodifiableMap(baked);
        blocksBakedInverse = Collections.unmodifiableMap(bakedInverse);
        this.blocks.clear();
    }

    @ApiStatus.Internal
    public synchronized void bake() {
        if (ModLifecycle.getLifecyclePhase() == ModLifecycle.PRE_INITIALIZATION) {
            bakeOriginal();
        } else {
            bakeVariants();
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static Ore merge(List<Ore> ores) {
        List<ResourceLocation> tags = new ArrayList<>();
        Set<ResourceLocation> tagsSet = new HashSet<>();
        Map<ResourceLocation, DetailedStone> blocks = new HashMap<>();
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

    public boolean isNotOriginal(ResourceKey<Stone> stone) {
        return !originalStones.contains(stone);
    }

    public static class Builder {
        private List<ResourceLocation> tags;
        private Map<ResourceLocation, DetailedStone> blocks;
        private Map<String, String> translations;
        private Set<ResourceKey<GroundType>> types;

        /**
         * @param tags tags which should be added to variants of this ore
         */
        public Builder setTags(List<ResourceLocation> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * @param blocks a map from block IDs to {@link DetailedStone}s which represents potential variant/stone
         *               pairings for this ore
         */
        public Builder setBlocks(Map<ResourceLocation, DetailedStone> blocks) {
            this.blocks = blocks;
            return this;
        }

        /**
         * @param translations a map from language codes (e.g. {@code "en_us"}) to translated names for this stone.
         *                     Translations will be prepended by the stone name, unless the translation contains
         *                     {@code "%s"}, in which case the stone name will be inserted at that location.
         */
        public Builder setTranslations(Map<String, String> translations) {
            this.translations = translations;
            return this;
        }

        /**
         * @param types the types of ground which this ore can generate in
         */
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
