package project.lib.scaffolding.parser;

import java.util.function.Function;

public class Parsed<T> {
    public Parsed(T value, Source rest) {
        this.value = value;
        this.rest = rest;
    }

    public final T value;
    public final Source rest;

    public <U> Parsed<U> map(Function<T, U> map) {
        return new Parsed<U>(map.apply(this.value), this.rest);
    }

    @Override
    public String toString() {
        return this.rest.offsetTotal() + ":(" + this.value.toString() + "," + this.rest.toString() + ")";
    }
}
