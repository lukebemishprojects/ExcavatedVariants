package com.github.lukebemish.excavated_variants.util;

import java.util.Objects;

public record Triple<A, B, C>(A first, B second, C last) {

    @Override
    public boolean equals(Object o) {
        return (o instanceof Triple p) && p.last().equals(last()) && p.second().equals(second()) && p.first().equals(first());
    }

    @Override
    public int hashCode() {
        return Objects.hash(first(), second(), last());
    }

    @Override
    public String toString() {
        return "Triple{" + first() + ", " + second() + ", " + last() + '}';
    }
}
