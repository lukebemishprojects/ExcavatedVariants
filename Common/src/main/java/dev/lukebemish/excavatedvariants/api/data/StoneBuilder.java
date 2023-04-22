package dev.lukebemish.excavatedvariants.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.lukebemish.excavatedvariants.data.BaseStone;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class StoneBuilder {
    private String id;
    private final Map<String, String> translations = new HashMap<>();
    private ResourceLocation blockId;
    private final List<String> types = new ArrayList<>();

    public Stone build() {
        if (id == null) {
            throw new IllegalStateException("Stone ID must be set");
        }
        if (blockId == null) {
            throw new IllegalStateException("Stone block ID must be set");
        }
        return new Stone(new BaseStone(id, Optional.empty(), blockId, types, translations));
    }

    public StoneBuilder id(String id) {
        if (this.id != null)
            throw new IllegalStateException("Stone ID already set");
        this.id = id;
        return this;
    }

    public StoneBuilder blockId(ResourceLocation blockId) {
        if (this.blockId != null)
            throw new IllegalStateException("Stone block ID already set");
        this.blockId = blockId;
        return this;
    }

    public StoneBuilder types(String... types) {
        this.types.addAll(List.of(types));
        return this;
    }

    public StoneBuilder types(List<String> types) {
        this.types.addAll(types);
        return this;
    }

    public StoneBuilder translations(Map<String, String> translations) {
        this.translations.putAll(translations);
        return this;
    }
}
