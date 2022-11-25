package project.lib.scaffolding.parser;

import java.util.function.Supplier;

@FunctionalInterface
public interface DiscardParser {

    public Parsed<Unit> parse(Source sequence);

    public default Parser<Unit> asParser() {
        return this::parse;
    }

    public default <T> Parser<T> coalesce(Supplier<T> factory) {
        return (input) -> {
            final var result = this.parse(input);
            if (result == null) {
                return null;
            }
            return new Parsed<>(factory.get(), result.rest);
        };
    }

    public default DiscardParser join(DiscardParser parser) {
        return (input) -> {
            final var result0 = this.parse(input);
            if (result0 == null) {
                return null;
            }
            final var result1 = parser.parse(result0.rest);
            if (result1 == null) {
                return null;
            }

            return new Parsed<>(Unit.instance, result1.rest);
        };
    }

    public default <U> Parser<U> join(Parser<U> parser) {
        return (input) -> {
            final var result0 = this.parse(input);
            if (result0 == null) {
                return null;
            }
            final var result1 = parser.parse(result0.rest);
            if (result1 == null) {
                return null;
            }

            return result1;
        };
    }
}
