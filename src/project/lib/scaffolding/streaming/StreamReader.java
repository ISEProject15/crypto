package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;

public interface StreamReader<T> {
    public void advance(int consumed) throws IOException;

    public Sequence<T> read(int least) throws IOException;

    public boolean completed();

    // -1 means undefined
    public default int prefferedReadSize() {
        return -1;
    }

    public default Sequence<T> read() throws IOException {
        return this.read(0);
    }

    public default Sequence<T> readTotally() throws IOException {
        final var defaultReadSize = 16;
        if (this.completed()) {
            return this.read();
        }

        while (true) {
            final var prefferedReadSize = this.prefferedReadSize();
            final var seq = this.read(prefferedReadSize < 0 ? defaultReadSize : prefferedReadSize);
            if (this.completed()) {
                return seq;
            }
        }
    }

    public default StreamReader<T> transform(Transformer<T> transformer) {
        return new TransformedStreamReader<>(this, transformer);
    }
}