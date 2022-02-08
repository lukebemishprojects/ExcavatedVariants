package excavated_variants.data;

import net.minecraft.resources.ResourceLocation;

public class BaseStone {
    public String id;
    public ResourceLocation texture_location;
    public String en_name;
    public ResourceLocation block_id;

    public BaseStone(String id, ResourceLocation texture_location, String en_name, ResourceLocation block_id) {
        this.id = id;
        this.texture_location = texture_location;
        this.en_name = en_name;
        this.block_id = block_id;
    }
}
