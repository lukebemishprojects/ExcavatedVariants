package com.github.lukebemish.excavated_variants.util;

import java.util.Objects;

public class Triple<A,B,C> extends Pair<A,C> {
    private final B second;
    public B second() {return second;}

    public Triple(A first, B second, C last) {
        super(first, last);
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Triple p)&&p.last().equals(last())&&p.second().equals(second())&&p.first().equals(first());
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
