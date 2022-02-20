package excavated_variants.worldgen;

import excavated_variants.ExcavatedVariants;
import excavated_variants.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OreGenMapSavedData extends SavedData {
    public static final String DATA_KEY = ExcavatedVariants.MOD_ID+":ore_replacement";
    public Map<Pair<Integer, Integer>, Integer> edgeCount = Collections.synchronizedMap(new HashMap<Pair<Integer, Integer>, Integer>());
    public Map<Pair<Integer, Integer>, Boolean> ranMap = Collections.synchronizedMap(new HashMap<Pair<Integer, Integer>, Boolean>());
    @Override
    public CompoundTag save(CompoundTag tag) {
        ArrayList<Integer> edge_1 = new ArrayList<>();
        ArrayList<Integer> edge_2 = new ArrayList<>();
        ArrayList<Integer> edge_3 = new ArrayList<>();
        ArrayList<Integer> ran_1 = new ArrayList<>();
        ArrayList<Integer> ran_2 = new ArrayList<>();
        ArrayList<Integer> ran_3 = new ArrayList<>();
        for (Pair<Integer,Integer> p : edgeCount.keySet()) {
            edge_1.add(p.first());
            edge_2.add(p.last());
            edge_3.add(edgeCount.get(p));
        }
        for (Pair<Integer,Integer> p : ranMap.keySet()) {
            ran_1.add(p.first());
            ran_2.add(p.last());
            ran_3.add(ranMap.get(p) ? 1 : 0);
        }
        tag.putIntArray("edge_1",edge_1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_2",edge_2.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("edge_3",edge_3.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_1",ran_1.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_2",ran_2.stream().mapToInt(Integer::intValue).toArray());
        tag.putIntArray("ran_3",ran_3.stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }

    public static OreGenMapSavedData load(CompoundTag tag) {
        OreGenMapSavedData data = new OreGenMapSavedData();
        int[] edge_1 = tag.getIntArray("edge_1");
        int[] edge_2 = tag.getIntArray("edge_2");
        int[] edge_3 = tag.getIntArray("edge_3");
        int[] ran_1 = tag.getIntArray("ran_1");
        int[] ran_2 = tag.getIntArray("ran_2");
        int[] ran_3 = tag.getIntArray("ran_3");
        if (edge_1.length == edge_2.length && edge_1.length == edge_3.length && ran_1.length == ran_2.length && ran_1.length == ran_3.length) {
            for (int i = 0; i < edge_1.length; i++) {
                data.edgeCount.put(new Pair<>(edge_1[i],edge_2[i]),edge_3[i]);
            }
            for (int i = 0; i < ran_1.length; i++) {
                data.ranMap.put(new Pair<>(ran_1[i],ran_2[i]),ran_3[i]!=0);
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
}
