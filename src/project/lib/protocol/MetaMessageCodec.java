package project.lib.protocol;

import java.io.IOException;

import project.lib.StringCodec;
import project.lib.scaffolding.collections.HList;
import project.lib.scaffolding.parser.DiscardParser;
import project.lib.scaffolding.parser.Parser;
import project.lib.scaffolding.parser.Parsers;
import project.lib.scaffolding.parser.Source;

public class MetaMessageCodec implements StringCodec<MetaMessage> {
    private static final Parser<String> id = Parsers.regex("[_a-zA-Z][_a-zA-Z0-9]*");
    private static final DiscardParser at = Parsers.regex("@").discard();
    private static final DiscardParser LF = Parsers.regex("\n").discard();
    public static final Parser<MetaMessage> parser = id.join(at).join(IonCodec.parser).join(LF)
            .map(MetaMessageCodec::createMetaMessage);

    private static MetaMessage createMetaMessage(HList<String, Ion> list) {
        final var body = list.head;
        final var id = list.rest;
        return MetaMessage.of(id, body);
    }

    public static final MetaMessageCodec instance = new MetaMessageCodec();

    @Override
    public MetaMessage decode(CharSequence sequence) {
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
    public void encode(Appendable buffer, MetaMessage value) {
        try {
            buffer.append(value.identity).append('@');
            IonCodec.instance.encode(buffer, value.body);
            buffer.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
