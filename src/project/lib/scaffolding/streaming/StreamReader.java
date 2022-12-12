package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;

public interface StreamReader<T> {
    public void advance(int consumed) throws IOException;

    public Sequence<T> read(int least) throws IOException;

    public boolean completed();

    public default Sequence<T> read() throws IOException {
        return this.read(0);
    }

    public default StreamReader<T> transform(Transformer<T> transformer) {
        return new TransformedStreamReader<>(this, transformer);
    }
}