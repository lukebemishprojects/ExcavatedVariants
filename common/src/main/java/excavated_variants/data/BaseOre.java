package excavated_variants.data;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BaseOre {
    public String id;
    public List<String> stone;
    public ResourceLocation texture_location;
    public ResourceLocation block_id;
    public String en_name;

    public BaseOre(String id, List<String> stone, ResourceLocation texture_location, ResourceLocation block_id, String en_name) {
        this.id = id;
        this.stone = stone;
        this.texture_location = texture_location;
        this.block_id = block_id;
        this.en_name = en_name;
    }
}
