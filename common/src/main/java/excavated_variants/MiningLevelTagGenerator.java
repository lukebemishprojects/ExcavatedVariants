package excavated_variants;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dynamic_asset_generator.api.ResettingSupplier;
import dynamic_asset_generator.api.ServerPrePackRepository;
import excavated_variants.data.BaseOre;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MiningLevelTagGenerator implements ResettingSupplier<InputStream> {
    private final String level;
    private final ArrayList<CheckPair> toCheck = new ArrayList<>();

    private String internal;

    private static record CheckPair(String full_id, String base_id) {

    }

    public MiningLevelTagGenerator(String level) {
        this.level = level;
    }

    public void add(String full_id, BaseOre ore) {
        toCheck.add(new CheckPair(full_id, ore.block_id.toString()));
    }

    @Override
    public InputStream get() {
        if (internal == null) {
            try {
                InputStream read = ServerPrePackRepository.getResource(new ResourceLocation("minecraft", "tags/blocks/needs_"+level+"_tool.json"));
                StringBuilder textBuilder = new StringBuilder();
                Reader reader = new BufferedReader(new InputStreamReader
                        (read, Charset.forName(StandardCharsets.UTF_8.name())));
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                String readStr = textBuilder.toString();
                JsonElement parser = JsonParser.parseString(readStr);
                boolean worked = false;
                ArrayList<String> to_add = new ArrayList<>();
                if (parser.isJsonObject()) {
                    JsonElement vals = parser.getAsJsonObject().get("values");
                    if (vals.isJsonArray()) {
                        for (JsonElement i : vals.getAsJsonArray()) {
                            if (i.isJsonPrimitive()) {
                                if (i.getAsJsonPrimitive().isString()) {
                                    String str = i.getAsJsonPrimitive().getAsString();
                                    for (CheckPair j : toCheck) {
                                        if (j.base_id.equals(str)) {
                                            to_add.add(j.full_id);
                                        }
                                    }
                                }
                            }
                        }
                        worked = true;
                    }
                }
                if (!worked) {
                    return null;
                }
                internal = "";
                for (String full_id : to_add) {
                    if (internal.length() >= 1) {
                        internal += ",";
                    }
                    internal += "\"" + ExcavatedVariants.MOD_ID + ":" + full_id + "\"";
                }
                internal = "{\"replace\":false,\"values\":["+internal+"]}";
            } catch (IOException e) {
                return null;
            }
        }
        String finalStr = internal;
        return new ByteArrayInputStream(finalStr.getBytes());
    }

    @Override
    public void reset() {
        internal = null;
    }
}
