package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;

public interface StreamReader {
    public void advance(int consumed) throws IOException;

    public Sequence<byte[]> read(int least) throws IOException;

    public boolean completed();

    public default Sequence<byte[]> read() throws IOException {
        return this.read(0);
    }
}
