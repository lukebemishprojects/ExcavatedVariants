package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.data.BaseStone;
import dynamic_asset_generator.api.ResettingSupplier;
import dynamic_asset_generator.client.api.ClientPrePackRepository;
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

    public static Supplier<InputStream> getConversionRecipe(String id,String out_id) {
        String json = "{\n" +
                "  \"type\": \"minecraft:crafting_shapeless\",\n" +
                "  \"group\": \""+id+"\",\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"tag\": \""+ExcavatedVariants.MOD_ID+":"+id+"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"result\": {\n" +
                "    \"item\": \""+out_id+"\",\n" +
                "    \"count\": 1\n" +
                "  }\n" +
                "}";
        return () -> {
            return new ByteArrayInputStream(json.getBytes());
        };
    }

    public static ResettingSupplier<InputStream> getBlockModel(BaseStone stone, String id) {
        return new ResettingSupplier<InputStream>() {
            String json;

            @Override
            public void reset() {
                json = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "0\"}}";
                String ore_location = ExcavatedVariants.MOD_ID + ":" + "block/" + id;
                InputStream read = null;
                try {
                    read = ClientPrePackRepository.getResource(new ResourceLocation(stone.rl_block_id.getNamespace(), "models/block/" + stone.rl_block_id.getPath() + ".json"));
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = new BufferedReader(new InputStreamReader
                            (read, Charset.forName(StandardCharsets.UTF_8.name())));
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                    String readStr = textBuilder.toString();
                    json = readStr;
                    int index = 0;
                    for (ResourceLocation texRL : stone.rl_texture_location) {
                        String stoneLoc = texRL.toString();
                        ResourceLocation stoneRl = ResourceLocation.of(stoneLoc, ':');
                        String stonePath = stoneRl.getPath();
                        if (stonePath.length() > 14 && stonePath.endsWith(".png") && stonePath.startsWith("textures/")) {
                            stoneLoc = stoneRl.getNamespace() + ":" + stonePath.substring(9, stonePath.length() - 4);
                            json = json.replace(stoneLoc + "\"", ore_location + index + "\"");
                        }
                        index++;
                    }
                } catch (IOException e) {
                    json = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "0\"}}";
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
