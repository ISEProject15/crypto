package project.lib.protocol.scaffolding.parser;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import project.lib.protocol.scaffolding.collections.HList;

// parser of T. this class MUST NOT have state.
@FunctionalInterface
public interface Parser<T> {
    public static <T> Parser<T> of(Parser<T> parser) {
        return parser;
    }

    public Parsed<T> parse(Source sequence);

    public default <U> Parser<U> map(Function<T, U> map) {
        return (input) -> {
            final var result = this.parse(input);
            if (result == null) {
                return null;
            }
            return result.map(map);
        };
    }

    public default <U> Parser<HList<T, U>> join(Parser<U> parser) {
        return (input) -> {
            final var result0 = this.parse(input);
            if (result0 == null) {
                return null;
            }
            final var result1 = parser.parse(result0.rest);
            if (result1 == null) {
                return null;
            }

            final var list = HList.of(result0.value, result1.value);
            return new Parsed<HList<T, U>>(list, result1.rest);
        };
    }

    // create a parser represents syntax of (this | other).
    // if you want to create parser returns TypeA or TypeB,
    // define super class or interface of both TypeA or TypeB,
    // then map parser parameter into the super type,
    // and finally join them with this method.
    public default Parser<T> or(Parser<T> other) {
        return (input) -> {
            final var result0 = this.parse(input);
            if (result0 != null) {
                return result0;
            }
            return other.parse(input);
        };
    }

    // create a parser represents syntax of (this*)
    public default Parser<Stream<T>> repeat() {
        return (input) -> {
            final var list = new ArrayList<T>();

            while (true) {
                final var result = this.parse(input);
                if (result == null)
                    break;

                list.add(result.value);
                input = result.rest;
            }
            return new Parsed<Stream<T>>(list.stream(), input);
        };
    }

    // create a parser represents syntax of (this, (separator, this)*, [separator])
    public default Parser<Stream<T>> separated(Parser<?> separator) {
        return (input) -> {
            final var list = new ArrayList<T>();
            final var first = this.parse(input);
            if (first == null) {
                return new Parsed<Stream<T>>(list.stream(), input);
            }
            list.add(first.value);
            input = first.rest;

            while (true) {
                final var sep = separator.parse(input);
                if (sep == null) {
                    break;
                }
                input = sep.rest;

                final var result = this.parse(input);
                if (result == null)
                    break;

                list.add(result.value);
                input = result.rest;
            }
            return new Parsed<Stream<T>>(list.stream(), input);
        };
    }

    // create a parser represents syntax of (this, (separator, this)*)
    public default Parser<Stream<T>> separatedExact(Parser<?> separator) {
        return (input) -> {
            final var list = new ArrayList<T>();
            final var first = this.parse(input);
            if (first == null) {
                return null;
            }
            list.add(first.value);
            input = first.rest;
            while (true) {
                final var sep = separator.parse(input);
                if (sep == null) {
                    break;
                }

                final var result = this.parse(sep.rest);
                if (result == null)
                    return null;
                list.add(result.value);
                input = result.rest;
            }
            return new Parsed<Stream<T>>(list.stream(), input);
        };
    }

    public default Parser<T> after(Consumer<Parsed<T>> action) {
        return (input) -> {
            final var result = this.parse(input);
            action.accept(result);
            return result;
        };
    }

    public default Parser<T> before(Consumer<Source> action) {
        return (input) -> {
            action.accept(input);
            final var result = this.parse(input);
            return result;
        };
    }
}