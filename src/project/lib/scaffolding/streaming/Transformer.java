package project.lib.scaffolding.streaming;

import java.io.Closeable;

import project.lib.scaffolding.collections.Sequence;

public interface Transformer<T> extends Closeable {
    public BufferWriter<T> writer();

    public Sequence<T> read();

    public void advance(int consumed);

    public boolean completed();

    public default long rest() {
        return this.read().length();
    }

    public default int prefferedInputSize() {
        return -1;
    }
}
