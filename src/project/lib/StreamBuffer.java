package project.lib;

import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;

public class StreamBuffer extends SegmentBuffer<byte[]> implements InletStream, OutletStream {
    public StreamBuffer() {
        this(SegmentBufferStrategy.defaultStrategy);
    }

    public StreamBuffer(SegmentBufferStrategy strategy) {
        super(strategy, byte[].class);
    }

    @Override
    public void close() {

    }
}
