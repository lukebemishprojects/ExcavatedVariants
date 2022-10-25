package io.github.lukebemish.excavated_variants.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.*;

public class CommentedCodec<A> implements Codec<A> {
    public static <T> CommentedCodec<T> of(Codec<T> codec) {
        return new CommentedCodec<>(codec);
    }

    private final Map<List<String>, String> comments;
    private final Codec<A> codec;

    protected CommentedCodec(Codec<A> wrapped) {
        this.codec = wrapped;
        this.comments = new HashMap<>();
    }

    @Override
    public <T1> DataResult<Pair<A, T1>> decode(DynamicOps<T1> ops, T1 input) {
        return codec.decode(ops, input);
    }

    @Override
    public <T1> DataResult<T1> encode(A input, DynamicOps<T1> ops, T1 prefix) {
        DataResult<T1> data = codec.encode(input, ops, prefix);
        if (ops instanceof CommentingOps<T1> commentingOps) {
            Optional<T1> result = data.result();
            if (result.isPresent()) {
                T1 object = result.get();
                comments.forEach((key, value) -> commentingOps.setComment(object, key, value));
                return DataResult.success(object);
            }
        }
        return data;
    }

    public CommentedCodec<A> comment(String comment, String... path) {
        comments.put(Arrays.stream(path).toList(), comment);
        return this;
    }

    public CommentedCodec<A> comment(String comment, List<String> path) {
        comments.put(path, comment);
        return this;
    }

    public CommentedCodec<A> comment(Map<List<String>, String> comments) {
        this.comments.putAll(comments);
        return this;
    }
}
