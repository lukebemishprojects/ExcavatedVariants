package excavated_variants.data;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BaseOre implements Cloneable {
    @Expose
    public String id;
    @Expose
    public List<String> stone;
    @Expose
    public String texture_location;
    public ResourceLocation rl_texture_location;
    @Expose
    public List<String> block_id;
    public List<ResourceLocation> rl_block_id;
    @Expose
    public String en_name;
    @Expose
    public List<String> types = List.of("stone");

    public BaseOre(String id, List<String> stone, ResourceLocation texture_location, List<ResourceLocation> block_id, String en_name, List<String> types) {
        this.id = id;
        this.stone = stone;
        this.rl_texture_location = texture_location;
        this.rl_block_id = block_id;
        this.en_name = en_name;
        if (types != null) {
            this.types = types;
        }
    }

    public BaseOre(String id, List<String> stone, String texture_location, List<String> block_id, String en_name, List<String> types) {
        this.id = id;
        this.stone = stone;
        this.texture_location = texture_location;
        this.block_id = block_id;
        this.en_name = en_name;
        if (types != null) {
            this.types = types;
        }
    }

    public BaseOre(String id, List<String> stone, ResourceLocation texture_location, ResourceLocation block_id, String en_name, List<String> types) {
        this(id,stone,texture_location,List.of(block_id),en_name,types);
    }

    public void setupBlockId() {
        if (rl_block_id==null) {
            this.rl_block_id = block_id.stream().map((x)->ResourceLocation.of(x,':')).toList();
        } else if (block_id==null) {
            this.block_id = rl_block_id.stream().map(ResourceLocation::toString).toList();
        }
        if (rl_texture_location == null) {
            this.rl_texture_location = ResourceLocation.of(texture_location, ':');
        } else if (texture_location==null) {
            this.texture_location = rl_texture_location.toString();
        }
    }

    public BaseOre clone() {
        try {
            return (BaseOre)super.clone();
        } catch (CloneNotSupportedException e) {
            // Why would this happen?
            return null;
        }
    }
}
