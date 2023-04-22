package dev.lukebemish.excavatedvariants.api;

import java.util.function.Consumer;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;

public interface DataProvider extends CompatPlugin {
    void provideOres(Consumer<Ore> oreConsumer, Consumer<Stone> stoneConsumer);
}
