/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.worldgen;

import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OreGenMapSavedData extends SavedData {
    public static final String DATA_KEY = ExcavatedVariants.MOD_ID + ":ore_replacement";
    private final Object2IntMap<ChunkKey> edgeCount;
    private final Set<ChunkKey> ranMap = Collections.synchronizedSet(new HashSet<>());

    public OreGenMapSavedData() {
        Object2IntMap<ChunkKey> edgeMap = new Object2IntOpenHashMap<>();
        edgeMap.defaultReturnValue(0);
        this.edgeCount = Object2IntMaps.synchronize(edgeMap);
    }

    public int getEdgeCount(ChunkKey chunkPos) {
        return edgeCount.getInt(chunkPos);
    }

    public void setEdgeCount(ChunkKey chunkPos, int count) {
        edgeCount.put(chunkPos, count);
        this.setDirty();
    }

    public void chunkRan(ChunkKey chunkPos) {
        ranMap.add(chunkPos);
        this.setDirty();
    }

    public boolean didChunkRun(ChunkKey chunkPos) {
        return ranMap.contains(chunkPos);
    }

    public record ChunkKey(int x, int z) {}

    private static OreGenMapSavedData load(CompoundTag tag) {
        OreGenMapSavedData data = new OreGenMapSavedData();
        int[] edge1 = tag.getIntArray("edge_1");
        int[] edge2 = tag.getIntArray("edge_2");
        int[] edge3 = tag.getIntArray("edge_3");
        int[] ran1 = tag.getIntArray("ran_1");
        int[] ran2 = tag.getIntArray("ran_2");
        if (edge1.length == edge2.length && edge1.length == edge3.length && ran1.length == ran2.length) {
            for (int i = 0; i < edge1.length; i++) {
                data.edgeCount.put(new ChunkKey(edge1[i], edge2[i]), edge3[i]);
            }
            for (int i = 0; i < ran1.length; i++) {
                data.ranMap.add(new ChunkKey(ran1[i], ran2[i]));
            }
        }
        return data;
    }

    private static OreGenMapSavedData create() {
        return new OreGenMapSavedData();
    }

    public static OreGenMapSavedData getOrCreate(ServerLevelAccessor world) {
        // (handled by a mixin)
        //noinspection DataFlowIssue
        return world.getLevel().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OreGenMapSavedData::create, OreGenMapSavedData::load, null),
                DATA_KEY
        );
    }

    @Override
    @NonNull
    public CompoundTag save(@NonNull CompoundTag tag) {
        ArrayList<Integer> edge1 = new ArrayList<>();
        ArrayList<Integer> edge2 = new ArrayList<>();
        ArrayList<Integer> edge3 = new ArrayList<>();
        ArrayList<Integer> ran1 = new ArrayList<>();
        ArrayList<Integer> ran2 = new ArrayList<>();
        for (ChunkKey p : edgeCount.keySet()) {
            edge1.add(p.x());
            edge2.add(p.z());
            edge3.add(edgeCount.getInt(p));
        }
        for (ChunkKey p : ranMap) {
            ran1.add(p.x());
            ran2.add(p.z());
        }
        tag.putIntArray("edge_1", edge1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_2", edge2.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_3", edge3.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_1", ran1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_2", ran2.stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }
}
