package excavated_variants;

import dynamic_asset_generator.api.ResettingSupplier;
import dynamic_asset_generator.client.api.ClientPrePackRepository;
import excavated_variants.data.BaseStone;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class JsonHelper {
    public static Supplier<InputStream> getBlockstate(String id) {
        String json = "{\"variants\": {\"\": {\"model\": \""+ExcavatedVariants.MOD_ID+":"+"block/"+id+"\"}}}";
        return () -> {
            return new ByteArrayInputStream(json.getBytes());
        };
    }

    public static ResettingSupplier<InputStream> getBlockModel(BaseStone stone, String id) {
        return new ResettingSupplier<InputStream>() {
            String json;

            @Override
            public void reset() {
                json = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "\"}}";
                String ore_location = ExcavatedVariants.MOD_ID + ":" + "block/" + id;
                InputStream read = null;
                try {
                    read = ClientPrePackRepository.getResource(new ResourceLocation(stone.block_id.getNamespace(), "models/block/" + stone.block_id.getPath() + ".json"));
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader
                            (read, Charset.forName(StandardCharsets.UTF_8.name())));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                    String readStr = textBuilder.toString();
                    String stoneLoc = stone.texture_location.toString();
                    ResourceLocation stoneRl = ResourceLocation.of(stoneLoc,':');
                    String stonePath = stoneRl.getPath();
                    if (stonePath.length() > 14 && stonePath.endsWith(".png") && stonePath.startsWith("textures/")) {
                        stoneLoc = stoneRl.getNamespace() + ":" + stonePath.substring(9, stonePath.length() - 4);
                        json = readStr.replace(stoneLoc + "\"", ore_location + "\"");
                    } else {
                        json = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "\"}}";
                    }
                } catch (IOException e) {
                    json = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "\"}}";
                }
            }

            @Override
            public InputStream get() {
                return new ByteArrayInputStream(json.getBytes());
            }
        };
    }

    public static Supplier<InputStream> getItemModel(String id) {
        String json = "{\"parent\": \""+ExcavatedVariants.MOD_ID+":"+"block/"+id+"\"}";
        return () -> {
            return new ByteArrayInputStream(json.getBytes());
        };
    }
}
