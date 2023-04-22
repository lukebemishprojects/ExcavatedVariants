package dev.lukebemish.excavatedvariants.api;

import java.util.function.Consumer;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;

/**
 * A listener which can provide ore and stone data.
 */
public interface DataProvider extends CommonListener {
    void provideOres(Consumer<Ore> oreConsumer, Consumer<Stone> stoneConsumer);
}
