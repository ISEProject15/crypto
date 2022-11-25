package project.lib.protocol;

import java.io.IOException;
import java.util.regex.Pattern;

import project.lib.scaffolding.collections.HList;
import project.lib.scaffolding.parser.DiscardParser;
import project.lib.scaffolding.parser.Parsed;
import project.lib.scaffolding.parser.Parser;
import project.lib.scaffolding.parser.Parsers;
import project.lib.scaffolding.parser.Source;

public class IonSerializer implements StringSerializer<Ion> {
    private static final Parser<Ion.Atom> atom = Parsers.regex("[^;&\n]*").map(IonBuilder::atom);
    private static final DiscardParser semicolon = Parsers.regex(";").discard();
    private static final DiscardParser ampasand = Parsers.regex("&").discard();
    private static final Parser<String> key = Parsers.regex("[_a-zA-Z0-9]+");
    public static final Parser<Ion> literal = IonSerializer::parseLiteral;
    private static final Parser<HList<String, Ion>> rule = key.join(literal);
    private static final Parser<Ion.Mapping> mapping = rule.separated(ampasand).map(list -> {
        final var mapping = IonBuilder.mapping();
        for (final var rule : list) {
            mapping.map(rule.rest, rule.head);
        }
        return mapping;
    });
    private static final Parser<Ion.Mapping> mappingLiteral = mapping.join(semicolon);
    private static final Pattern arrayAtomPattern = Pattern.compile("([^;&\n|:=][^;&\n]*)?");
    private static final Parser<Ion.Atom> arrayAtom = Parsers.regex(arrayAtomPattern).map(IonBuilder::atom);
    private static final Parser<Ion> arrayElement = literal.or(arrayAtom.map(x -> (Ion) x));
    private static final Parser<Ion.Array> arrayLiteral = arrayElement.separated(ampasand).join(semicolon)
            .map(IonBuilder::array);

    private static Parsed<Ion> parseLiteral(Source input) {
        if (input.length() <= 0) {
            return null;
        }
        final var head = input.charAt(0);
        final var rest = input.subSequence(1);
        return switch (head) {
            case '|' -> arrayLiteral.parse(rest).map(x -> (Ion) x);
            case ':' -> mappingLiteral.parse(rest).map(x -> (Ion) x);
            case '=' -> atom.parse(rest).map(x -> (Ion) x);
            default -> null;
        };
    }

    public static final Parser<Ion> parser = literal.or(mapping.map(x -> (Ion) x));
    public static final IonSerializer instance = new IonSerializer();

    @Override
    public Ion deserialize(CharSequence sequence) {
        final var result = parser.parse(Source.from(sequence));
        if (result == null) {
            return null;
        }
        return result.value;
    }

    @Override
    public void serialize(Appendable buffer, Ion value) throws IOException {
        switch (value.TYPE) {
            case Ion.MAPPING -> serializeMapping(buffer, value.asMapping());
            default -> serializeNonRoot(buffer, value);
        }
    }

    private static void serializeNonRoot(Appendable buffer, Ion value) throws IOException {
        switch (value.TYPE) {
            case Ion.ATOM -> {
                buffer.append('=').append(value.asAtom().text());
            }
            case Ion.MAPPING -> {
                buffer.append(':');
                serializeMapping(buffer, value.asMapping());
                buffer.append(';');
            }
            case Ion.ARRAY -> {
                final var array = value.asArray();
                buffer.append('|');
                var initial = true;
                for (final var item : array) {
                    if (initial) {
                        initial = false;
                    } else {
                        buffer.append('&');
                    }
                    switch (item.TYPE) {
                        case Ion.ATOM:
                            final var atom = item.asAtom();
                            if (arrayAtomPattern.matcher(atom.text()).matches()) {
                                buffer.append(atom.text());
                                break;
                            }
                        default:
                            serializeNonRoot(buffer, item);
                            break;
                    }
                }
                buffer.append(';');
            }
        }
    }

    private static void serializeMapping(Appendable buffer, Ion.Mapping mapping) throws IOException {
        var initial = true;
        for (final var rule : mapping.entries()) {
            if (initial) {
                initial = false;
            } else {
                buffer.append('&');
            }
            final var key = rule.getKey();
            final var val = rule.getValue();

            buffer.append(key);
            serializeNonRoot(buffer, val);
        }
    }
}