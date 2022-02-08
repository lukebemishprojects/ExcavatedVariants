package excavated_variants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

public class TagBuilder {
    private String internal = "";

    public void add(String full_id) {
        if (internal.length() >= 1) {
            internal += ",";
        }
        internal += "\""+ExcavatedVariants.MOD_ID+":"+full_id+"\"";
    }

    public Supplier<InputStream> build() {
        String json = "{\"replace\":false,\"values\":["+internal+"]}";
        return () -> {
            return new ByteArrayInputStream(json.getBytes());
        };
    }
}
