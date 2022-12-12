package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;

class TransformedStreamReader<T> implements StreamReader<T> {
    TransformedStreamReader(StreamReader<T> reader, Transformer<T> transformer) {
        this.reader = reader;
        this.transformer = transformer;
    }

    private final StreamReader<T> reader;
    private final Transformer<T> transformer;

    @Override
    public void advance(int consumed) throws IOException {
        this.transformer.advance(consumed);
    }

    @Override
    public Sequence<T> read(int least) throws IOException {
        final var transformer = this.transformer;
        final var writer = transformer.writer();
        while (transformer.rest() < least) {
            final var len = Math.max(transformer.prefferedInputSize(), least);
            final var raw = reader.read(len);
            final var completed = reader.completed();
            writer.write(raw, completed);
            if (completed) {
                break;
            }
        }
        return transformer.read();
    }

    @Override
    public boolean completed() {
        return transformer.completed();
    }

}