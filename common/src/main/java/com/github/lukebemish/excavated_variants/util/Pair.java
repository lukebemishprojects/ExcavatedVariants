package com.github.lukebemish.excavated_variants.util;

import java.util.Objects;

public class Pair<A,B> {
    private final A first;
    private final B last;

    public Pair(A first, B last) {
        this.first = first;
        this.last = last;
    }

    public A first() {return first;}
    public B last() {return last;}


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Pair p)&&p.last().equals(last())&&p.first().equals(first());
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
