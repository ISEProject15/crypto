package project.lib.protocol;

import java.io.IOException;

import project.lib.scaffolding.collections.HList;
import project.lib.scaffolding.parser.Parser;
import project.lib.scaffolding.parser.Parsers;
import project.lib.scaffolding.parser.Source;

public class MetaMessageSerializer implements Serializer<MetaMessage> {
    private static final Parser<String> id = Parsers.regex("[_a-zA-Z][_a-zA-Z0-9]*");
    private static final Parser<String> at = Parsers.regex("@");
    private static final Parser<String> LF = Parsers.regex("\n");
    public static final Parser<MetaMessage> parser = id.join(at).join(IonSerializer.parser).join(LF)
            .map(MetaMessageSerializer::createMetaMessage);

    private static MetaMessage createMetaMessage(HList<HList<HList<String, String>, Ion>, String> list) {
        final var body = list.rest.head;
        final var id = list.rest.rest.rest;
        return MetaMessage.of(id, body);
    }

    public static final MetaMessageSerializer instance = new MetaMessageSerializer();

    @Override
    public MetaMessage deserialize(CharSequence sequence) {
        final var result = parser.parse(Source.from(sequence));
        if (result == null) {
            return null;
        }
        if (!result.rest.isEmpty()) {// sequence has too much text
            return null;
        }
        return result.value;
    }

    @Override
    public void serialize(Appendable buffer, MetaMessage value) {
        try {
            buffer.append(value.identity).append('@');
            IonSerializer.instance.serialize(buffer, value.body);
            buffer.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
