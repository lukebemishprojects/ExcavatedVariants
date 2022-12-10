package dev.lukebemish.excavatedvariants.codecs;

import com.mojang.serialization.DynamicOps;

import java.util.List;

public interface CommentingOps<T> extends DynamicOps<T> {
    void setComment(T input, List<String> path, String comment);
}
