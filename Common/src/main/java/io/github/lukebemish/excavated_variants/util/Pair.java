package io.github.lukebemish.excavated_variants.util;

import java.util.Objects;

public record Pair<A, B>(A first, B last) {


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Pair p) && p.last().equals(last()) && p.first().equals(first());
    }

    @Override
    public String toString() {
        return "Pair{" + first() + ", " + last() + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(first(), last());
    }
}
