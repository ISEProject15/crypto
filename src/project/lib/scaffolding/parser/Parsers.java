package project.lib.scaffolding.parser;

import java.util.Optional;
import java.util.regex.Pattern;

public abstract class Parsers {
    public static Parser<String> regex(Pattern pattern) {
        return (input) -> {
            final var matcher = pattern.matcher(input);
            if (!matcher.lookingAt()) {
                return null;
            }

            return new Parsed<String>(
                    matcher.group(),
                    input.subSequence(matcher.end()));
        };
    }

    public static Parser<String> regex(String pattern) {
        return regex(Pattern.compile(pattern));
    }

    public static <T> Parser<Optional<T>> optional(Parser<T> parser) {
        return (input) -> {
            final var result = parser.parse(input);

            if (result == null) {
                return new Parsed<Optional<T>>(Optional.empty(), input);
            }

            return result.map(Optional::of);
        };
    }
}
