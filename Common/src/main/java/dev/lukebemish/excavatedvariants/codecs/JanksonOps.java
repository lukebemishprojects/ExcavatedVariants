package dev.lukebemish.excavatedvariants.codecs;

import blue.endless.jankson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JanksonOps implements CommentingOps<JsonElement> {
    public static final JanksonOps INSTANCE = new JanksonOps();

    @Override
    public void setComment(JsonElement input, List<String> path, String comment) {
        try {
            if (input instanceof JsonObject object) {
                if (path.size() == 1) {
                    object.setComment(path.get(0), comment);
                }
                List<String> remaining = new ArrayList<>(path);
                String single = remaining.remove(0);
                JsonElement child = object.get(single);
                if (child == null) {
                    throw new UnsupportedOperationException("Cannot set comment on nonexistent object: " + single);
                }
                setComment(child, remaining, comment);
                return;
            }
        } catch (UnsupportedOperationException ignored) {
            // falls through to new unsupported op exception.
        }
        throw new UnsupportedOperationException(String.format("Cannot set comments on children with path %s of object %s", path, input));
    }

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
        if (input instanceof JsonObject)
            return convertMap(outOps, input);
        if (input instanceof JsonArray)
            return convertList(outOps, input);
        if (input instanceof JsonNull)
            return outOps.empty();
        if (input instanceof JsonPrimitive primitive) {
            Object value = primitive.getValue();
            if (value instanceof String string)
                return outOps.createString(string);
            if (value instanceof Number number)
                return outOps.createNumeric(number);
            if (value instanceof Boolean bool)
                return outOps.createBoolean(bool);
        }
        throw new UnsupportedOperationException("JanksonOps was unable to convert a value: " + input);
    }

    @Override
    public DataResult<Number> getNumberValue(JsonElement input) {
        if (input instanceof JsonPrimitive primitive) {
            Object value = primitive.getValue();
            if (value instanceof Number number)
                return DataResult.success(number);
            if (value instanceof Boolean bool)
                return DataResult.success(Boolean.TRUE.equals(bool) ? 1 : 0);
        }
        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public JsonElement createNumeric(Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public DataResult<String> getStringValue(JsonElement input) {
        if (input instanceof JsonPrimitive primitive) {
            Object value = primitive.getValue();
            if (value instanceof String string)
                return DataResult.success(string);
        }
        return DataResult.error(() -> "Not a string: " + input);
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<JsonElement> mergeToList(JsonElement list, JsonElement value) {
        if (list instanceof JsonArray || list == empty()) {
            JsonArray result = new JsonArray();
            if (list != empty()) {
                JsonArray array = (JsonArray) list;
                result.addAll(array);
            }
            result.add(value);
            return DataResult.success(result);
        }
        return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
    }

    @Override
    public DataResult<JsonElement> mergeToList(final JsonElement list, final List<JsonElement> values) {
        if (list instanceof JsonArray || list == empty()) {
            JsonArray result = new JsonArray();
            if (list != empty()) {
                JsonArray array = (JsonArray) list;
                result.addAll(array);
            }
            result.addAll(values);
            return DataResult.success(result);
        }
        return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement map, JsonElement key, JsonElement value) {
        if (!(map instanceof JsonObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        if (!(key instanceof JsonPrimitive primitive) || !(primitive.getValue() instanceof String string)) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }
        JsonObject output = new JsonObject();
        if (map != empty()) {
            JsonObject jsonObject = (JsonObject) map;
            output.putAll(jsonObject);
        }
        output.put(string, value);
        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement map, MapLike<JsonElement> values) {
        if (!(map instanceof JsonObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        JsonObject output = new JsonObject();
        if (map != empty()) {
            JsonObject jsonObject = (JsonObject) map;
            output.putAll(jsonObject);
        }
        List<JsonElement> missed = new ArrayList<>();
        values.entries().forEach(entry -> {
            if (entry.getFirst() instanceof JsonPrimitive primitive && primitive.getValue() instanceof String string)
                output.put(string, entry.getSecond());
            else missed.add(entry.getFirst());
        });

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }

    @Override
    public DataResult<MapLike<JsonElement>> getMap(final JsonElement input) {
        if (!(input instanceof JsonObject object)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(new MapLike<>() {
            @Nullable
            @Override
            public JsonElement get(JsonElement key) {
                if (key instanceof JsonPrimitive primitive && primitive.getValue() instanceof String string) return get(string);
                return null;
            }

            @Nullable
            @Override
            public JsonElement get(String key) {
                JsonElement element = object.get(key);
                if (element instanceof JsonNull) {
                    return null;
                }
                return element;
            }

            @Override
            public Stream<Pair<JsonElement, JsonElement>> entries() {
                return object.entrySet().stream().map(e -> Pair.of(new JsonPrimitive(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(JsonElement input) {
        if (input instanceof JsonObject object) {
            return DataResult.success(object.entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue() instanceof JsonNull ? null : entry.getValue())));
        }
        return DataResult.error(() -> "Not a JSON object: " + input);
    }

    @Override
    public JsonElement createMap(Stream<Pair<JsonElement, JsonElement>> map) {
        JsonObject result = new JsonObject();
        map.forEach(p -> {
            if (!(p.getFirst() instanceof JsonPrimitive primitive && primitive.getValue() instanceof String string))
                throw new UnsupportedOperationException(p.getFirst().getClass().getSimpleName());
            result.put(string, p.getSecond());
        });
        return result;
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(JsonElement input) {
        if (input instanceof JsonArray array) {
            return DataResult.success(array.stream().map(e -> e instanceof JsonNull ? null : e));
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public JsonElement createList(Stream<JsonElement> input) {
        JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
    }

    @Override
    public JsonElement remove(JsonElement input, String key) {
        if (input instanceof JsonObject object) {
            JsonObject result = new JsonObject();
            result.putAll(object);
            result.remove(key);
            return result;
        }
        return input;
    }

    public String toString() {
        return "Jankson";
    }
}