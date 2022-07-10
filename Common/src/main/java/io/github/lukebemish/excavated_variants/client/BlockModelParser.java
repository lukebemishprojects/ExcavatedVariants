package io.github.lukebemish.excavated_variants.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lukebemish.dynamic_asset_generator.client.api.ClientPrePackRepository;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BlockModelParser {
    public String parent;
    public Map<String, String> textures;
    public JsonArray elements;

    @Override
    public String toString() {
        return "BlockModelParser{" +
                "textures=" + textures +
                ", elements=" + elements +
                '}';
    }

    public void replaceTexture(ResourceLocation in, ResourceLocation out) {
        for (String k : textures.keySet()) {
            var rl = ResourceLocation.of(textures.get(k),':');
            if (rl.equals(in)) textures.put(k,out.toString());
        }
    }

    public static BlockModelParser readModel(Reader reader) {
        BlockModelParser parser = BlockStateAssembler.GSON.fromJson(reader, BlockModelParser.class);
        return processModel(parser);
    }
    public static BlockModelParser readModel(String string) {
        BlockModelParser parser = BlockStateAssembler.GSON.fromJson(string, BlockModelParser.class);
        return processModel(parser);
    }
    public static BlockModelParser readModel(JsonObject object) {
        BlockModelParser parser = BlockStateAssembler.GSON.fromJson(object, BlockModelParser.class);
        return processModel(parser);
    }

    private static BlockModelParser processModel(BlockModelParser modelParser) {
        // For parsing out Forge weirdness later if I run into issues...
        return modelParser;
    }



    public void addOverlay(int index, ResourceLocation overlay_loc) {
        if (elements == null) {
            BlockModelParser finder = this;
            boolean found = false;
            while (!found) {
                ResourceLocation parentRl = ResourceLocation.of(finder.parent,':');
                try (var read = ClientPrePackRepository.getResource(new ResourceLocation(parentRl.getNamespace(), "models/" + parentRl.getPath() + ".json"))) {
                    BlockModelParser parentModel = readModel(new BufferedReader(new InputStreamReader(read, StandardCharsets.UTF_8)));
                    if (parentModel.elements!=null) {
                        elements = parentModel.elements;
                        found = true;
                    } else {
                        if (parentModel.parent == null) {
                            elements = new JsonArray();
                            found = true;
                        } else {
                            finder = parentModel;
                        }
                    }
                } catch (IOException e) {
                    elements = new JsonArray();
                    found = true;
                    ExcavatedVariants.LOGGER.warn("Could not find parent model {}",parentRl.toString());
                }
            }
        }
        String initial = """
                {
                      "from": [ 0, 0, 0 ],
                      "to": [ 16, 16, 16 ],
                      "faces": {
                        "down":  { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "down" },
                        "up":    { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "up" },
                        "north": { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "north" },
                        "south": { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "south" },
                        "west":  { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "west" },
                        "east":  { "uv": [ 0, 0, 16, 16 ], "texture": "#overlay", "cullface": "east" }
                      }
                    }""".replace("#overlay","#exc_var_overlay"+index);

        textures.put("exc_var_overlay"+index,overlay_loc.toString());
        elements.add(BlockStateAssembler.GSON.fromJson(initial, JsonObject.class));
    }
}
