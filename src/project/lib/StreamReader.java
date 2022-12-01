package project.lib;

import project.lib.scaffolding.collections.Sequence;

import java.io.IOException;

import project.lib.scaffolding.collections.SegmentBufferStrategy;

public class StreamReader {
    public static StreamReader create(InletStream inlet) {
        return new StreamReader(SegmentBufferStrategy.defaultStrategy, inlet);
    }

    private StreamReader(SegmentBufferStrategy strategy, InletStream inlet) {
        this.buffer = new StreamBuffer(strategy);
        this.inlet = inlet;
        this.finished = false;
        this.examined = 0;
    }

    private final InletStream inlet;
    private final StreamBuffer buffer;
    private boolean finished;
    private int examined;

    public void advance(int consumed, int examined) throws IOException {
        this.buffer.discard(consumed);
        this.examined = examined;
    }

    public byte[] collect() {
        return this.buffer.toArray();
    }

    public int available() {
        return this.buffer.length();
    }

    public void read() {
        this.read(-1);
    }

    public Sequence<byte[]> read(int least) {
        return this.buffer;
    }

    public boolean finished() {
        return this.finished;
    }
}
