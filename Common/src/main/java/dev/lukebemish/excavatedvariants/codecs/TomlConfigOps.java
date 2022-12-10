package dev.lukebemish.excavatedvariants.codecs;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.*;
import java.util.stream.Stream;

public class TomlConfigOps implements CommentingOps<Object> {

    public static final TomlConfigOps INSTANCE = new TomlConfigOps();

    protected Config newConfig() {
        return TomlFormat.newConfig();
    }

    @Override
    public Object empty() {
        return NullObject.NULL_OBJECT;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if (input instanceof Config)
            return this.convertMap(outOps, input);
        if (input instanceof Collection)
            return this.convertList(outOps, input);
        if (input == null || input instanceof NullObject)
            return outOps.empty();
        if (input instanceof Enum<?> enumValue)
            return outOps.createString(enumValue.name());
        if (input instanceof Number number)
            return outOps.createNumeric(number);
        if (input instanceof String string)
            return outOps.createString(string);
        if (input instanceof Boolean bool)
            return outOps.createBoolean(bool);
        throw new UnsupportedOperationException("NightConfigOps was unable to convert a value: " + input);
    }

    @Override
    public DataResult<Number> getNumberValue(Object i) {
        return i instanceof Number n
                ? DataResult.success(n)
                : DataResult.error("Not a number: " + i);
    }

    @Override
    public Object createNumeric(Number i) {
        return i;
    }

    @Override
    public DataResult<String> getStringValue(Object input) {
        return (input instanceof Config || input instanceof Collection) ?
                DataResult.error("Not a string: " + input) :
                DataResult.success(String.valueOf(input));
    }

    @Override
    public Object createString(String value) {
        return value;
    }

    @Override
    public DataResult<Object> mergeToList(Object list, Object value) {
        if (!(list instanceof Collection) && list != this.empty()) {
            return DataResult.error("mergeToList called with not a list: " + list, list);
        }
        final Collection<Object> result = new ArrayList<>();
        if (list != this.empty()) {
            @SuppressWarnings("unchecked")
            Collection<Object> listAsCollection = (Collection<Object>)list;
            result.addAll(listAsCollection);
        }
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if (!(map instanceof Config) && map != this.empty()) {
            return DataResult.error("mergeToMap called with not a map: " + map, map);
        }
        DataResult<String> stringResult = this.getStringValue(key);
        Optional<DataResult.PartialResult<String>> badResult = stringResult.error();
        if (badResult.isPresent()) {
            return DataResult.error("key is not a string: " + key, map);
        }
        return stringResult.flatMap(s ->{

            final Config output = newConfig();
            if (map != this.empty())
            {
                Config oldConfig = (Config)map;
                output.addAll(oldConfig);
            }
            output.add(s, value);
            return DataResult.success(output);
        });
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        if (!(input instanceof final Config config)) {
            return DataResult.error("Not a Config: " + input);
        }
        return DataResult.success(config.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> map) {
        final Config result = newConfig();
        map.forEach(p -> result.add(this.getStringValue(p.getFirst()).getOrThrow(false, s -> {}), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object input) {
        if (input instanceof Collection)
        {
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>)input;
            return DataResult.success(collection.stream());
        }
        return DataResult.error("Not a collection: " + input);
    }

    @Override
    public Object createList(Stream<Object> input) {
        return input.toList();
    }

    @Override
    public Object remove(Object input, String key) {
        if (input instanceof Config oldConfig)
        {
            final Config result = newConfig();
            oldConfig.entrySet().stream()
                    .filter(entry -> !Objects.equals(entry.getKey(), key))
                    .forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return result;
        }
        return input;
    }

    @Override
    public void setComment(Object input, List<String> path, String comment) {
        if (input instanceof CommentedConfig config) {
            config.setComment(path, comment);
        }
    }
}
