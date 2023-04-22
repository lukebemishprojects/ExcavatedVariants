package dev.lukebemish.excavatedvariants.api;

import java.util.List;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;

public interface DataReceiver extends CompatPlugin {
    void receiveData(List<Pair<Ore, Set<Stone>>> data);
}
