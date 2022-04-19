package com.github.lukebemish.excavated_variants.client;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class BlockModelParser {
    public Map<String, String> textures;
    public List<Object> elements;

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

    public void addOverlay(int index, ResourceLocation overlay_loc) {
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
