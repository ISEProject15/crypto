package project.lib.scaffolding.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import project.lib.scaffolding.collections.HList;

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
    public default Parser<List<T>> repeat() {
        return (input) -> {
            final var list = new ArrayList<T>();

            while (true) {
                final var result = this.parse(input);
                if (result == null)
                    break;

                list.add(result.value);
                input = result.rest;
            }
            return new Parsed<List<T>>(list, input);
        };
    }

    // create a parser represents syntax of [this, (separator, this)*]
    // but less than or equal to upper times. if upper less than or equal to 0,
    // upper is ignored.
    public default Parser<List<T>> separatedMost(Parser<?> separator, int upper) {
        return (input) -> {
            final var list = new ArrayList<T>();
            final var first = this.parse(input);
            if (first == null) {
                return new Parsed<List<T>>(list, input);
            }
            list.add(first.value);
            input = first.rest;

            while (true) {
                final var sep = separator.parse(input);
                if (sep == null) {
                    break;
                }

                final var result = this.parse(sep.rest);
                if (result == null) {
                    break;
                }

                list.add(result.value);
                input = result.rest;

                if (upper > 0 && list.size() > upper) {
                    return null;
                }
            }

            return new Parsed<List<T>>(list, input);
        };
    }

    // create a parser represents syntax of (this, (separator, this)*)
    // if lower less than or equal to 0, lower is ignored.
    // if upper less than or equal to 0, upper is ignored.
    public default Parser<List<T>> separated(Parser<?> separator, int lower, int upper) {
        return separatedMost(separator, upper).map(x -> {
            if (lower > 0 && x.size() < lower) {
                return null;
            }
            return x;
        });
    }

    public default Parser<T> after(java.util.function.Consumer<Parsed<T>> action) {
        return (input) -> {
            final var result = this.parse(input);
            if (result != null) {
                action.accept(result);
            }
            return result;
        };
    }

    public default Parser<T> before(java.util.function.Consumer<Source> action) {
        return (input) -> {
            action.accept(input);
            return this.parse(input);
        };
    }

    public default Parser<T> inspect(java.util.function.Consumer<Source> actBefore,
            java.util.function.Consumer<T> actAfter) {
        return (input) -> {
            actBefore.accept(input);
            final var result = this.parse(input);
            if (result != null) {
                actAfter.accept(result.value);
            }
            return result;
        };
    }
}