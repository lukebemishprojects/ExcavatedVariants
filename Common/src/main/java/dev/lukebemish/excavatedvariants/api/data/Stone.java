package dev.lukebemish.excavatedvariants.api.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.lukebemish.excavatedvariants.impl.data.BaseStone;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class Stone {
    private final BaseStone stone;

    @ApiStatus.Internal
    public Stone(@NotNull BaseStone stone) {
        this.stone = stone;
    }

    public String getId() {
        return stone.id;
    }

    public Map<String, String> getTranslations() {
        return Collections.unmodifiableMap(stone.lang);
    }

    public ResourceLocation getBlockId() {
        return stone.blockId;
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(stone.types);
    }

    @ApiStatus.Internal
    public BaseStone getBase() {
        return stone;
    }
}
