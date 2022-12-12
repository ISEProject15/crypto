package project.lib.scaffolding.streaming;

import project.lib.scaffolding.collections.Sequence;

public interface Transformer {
    public BufferWriter<byte[]> writer();

    public Sequence<byte[]> read();

    public void advance(int consumed);

    public boolean completed();
}
