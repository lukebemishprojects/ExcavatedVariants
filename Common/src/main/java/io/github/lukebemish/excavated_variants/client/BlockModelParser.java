package io.github.lukebemish.excavated_variants.client;

import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.Map;

public class BlockModelParser {
    public String parent;
    public Map<String, String> textures;
    public JsonArray elements;

    // Really, Forge? This is going to be painful...
    @Nullable
    public Map<String, BlockModelParser> children;

    public String render_type;

    public static BlockModelParser readModel(Reader reader) {
        BlockModelParser parser = BlockStateAssembler.GSON.fromJson(reader, BlockModelParser.class);
        return processModel(parser);
    }

    public static BlockModelParser readModel(String string) {
        BlockModelParser parser = BlockStateAssembler.GSON.fromJson(string, BlockModelParser.class);
        return processModel(parser);
    }

    private static BlockModelParser processModel(BlockModelParser modelParser) {
        // For parsing out Forge weirdness later if I run into issues...
        return modelParser;
    }

    @Override
    public String toString() {
        return "BlockModelParser{" +
                "textures=" + textures +
                ", elements=" + elements +
                '}';
    }

    public void replaceTexture(ResourceLocation in, ResourceLocation out) {
        for (String k : textures.keySet()) {
            var rl = ResourceLocation.of(textures.get(k), ':');
            if (rl.equals(in)) textures.put(k, out.toString());
        }
    }
}
