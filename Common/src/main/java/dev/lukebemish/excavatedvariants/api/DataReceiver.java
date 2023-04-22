package dev.lukebemish.excavatedvariants.api;

import java.util.List;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;

/**
 * A listener which recieves ore and stone combination data after it has been loaded and processed.
 */
public interface DataReceiver extends CommonListener {
    void receiveData(List<Pair<Ore, Set<Stone>>> data);
}
