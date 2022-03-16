package com.github.lukebemish.excavated_variants.data;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BaseStone {
    @Expose
    public String id;
    @Expose
    public List<String> texture_location;
    public List<ResourceLocation> rl_texture_location;
    @Expose
    public String en_name;
    @Expose
    public String block_id;
    public ResourceLocation rl_block_id;
    @Expose
    public List<String> type = List.of("stone");

    public BaseStone(String id, ResourceLocation texture_location, String en_name, ResourceLocation block_id, List<String> type) {
        this.id = id;
        this.rl_texture_location = List.of(texture_location);
        this.en_name = en_name;
        this.rl_block_id = block_id;
        if (type != null) {
            this.type = type;
        }
    }

    public BaseStone(String id, String texture_location, String en_name, String block_id, List<String> type) {
        this.id = id;
        this.texture_location = List.of(texture_location);
        this.en_name = en_name;
        this.block_id = block_id;
        if (type != null) {
            this.type = type;
        }
    }

    public BaseStone(String id, List<String> texture_location, String en_name, String block_id, List<String> type) {
        this.id = id;
        this.texture_location = texture_location;
        this.en_name = en_name;
        this.block_id = block_id;
        if (type != null) {
            this.type = type;
        }
    }

    public void setupBlockId() {
        if (rl_block_id == null) {
            this.rl_block_id = ResourceLocation.of(block_id, ':');
        } else if (block_id==null) {
            this.block_id = rl_block_id.toString();
        }
        if (rl_texture_location == null) {
            this.rl_texture_location = texture_location.stream().map((x)->ResourceLocation.of(x, ':')).toList();
        } else if (texture_location==null) {
            this.texture_location = rl_texture_location.stream().map(ResourceLocation::toString).toList();
        }
    }
}
