package dev.lukebemish.excavatedvariants.impl.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

public class OreGenMapSavedData extends SavedData {
    public static final String DATA_KEY = ExcavatedVariants.MOD_ID + ":ore_replacement";
    public Map<Pair<Integer, Integer>, Integer> edgeCount = Collections.synchronizedMap(new HashMap<>());
    public Map<Pair<Integer, Integer>, Boolean> ranMap = Collections.synchronizedMap(new HashMap<>());

    public static OreGenMapSavedData load(CompoundTag tag) {
        OreGenMapSavedData data = new OreGenMapSavedData();
        int[] edge1 = tag.getIntArray("edge_1");
        int[] edge2 = tag.getIntArray("edge_2");
        int[] edge3 = tag.getIntArray("edge_3");
        int[] ran1 = tag.getIntArray("ran_1");
        int[] ran2 = tag.getIntArray("ran_2");
        int[] ran3 = tag.getIntArray("ran_3");
        if (edge1.length == edge2.length && edge1.length == edge3.length && ran1.length == ran2.length && ran1.length == ran3.length) {
            for (int i = 0; i < edge1.length; i++) {
                data.edgeCount.put(new Pair<>(edge1[i], edge2[i]), edge3[i]);
            }
            for (int i = 0; i < ran1.length; i++) {
                data.ranMap.put(new Pair<>(ran1[i], ran2[i]), ran3[i] != 0);
            }
        }
        return data;
    }

    public static OreGenMapSavedData create() {
        return new OreGenMapSavedData();
    }

    public static OreGenMapSavedData getOrCreate(ServerLevelAccessor world) {
        return world.getLevel().getDataStorage().computeIfAbsent(OreGenMapSavedData::load, OreGenMapSavedData::create, DATA_KEY);
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        ArrayList<Integer> edge1 = new ArrayList<>();
        ArrayList<Integer> edge2 = new ArrayList<>();
        ArrayList<Integer> edge3 = new ArrayList<>();
        ArrayList<Integer> ran1 = new ArrayList<>();
        ArrayList<Integer> ran2 = new ArrayList<>();
        ArrayList<Integer> ran3 = new ArrayList<>();
        for (Pair<Integer, Integer> p : edgeCount.keySet()) {
            edge1.add(p.getFirst());
            edge2.add(p.getSecond());
            edge3.add(edgeCount.get(p));
        }
        for (Pair<Integer, Integer> p : ranMap.keySet()) {
            ran1.add(p.getFirst());
            ran2.add(p.getSecond());
            ran3.add(Boolean.TRUE.equals(ranMap.get(p)) ? 1 : 0);
        }
        tag.putIntArray("edge_1", edge1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_2", edge2.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_3", edge3.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_1", ran1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_2", ran2.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_3", ran3.stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }
}
