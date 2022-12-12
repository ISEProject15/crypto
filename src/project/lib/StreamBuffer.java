package project.lib;

import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;

public class StreamBuffer extends SegmentBuffer<byte[]> implements InletStream, OutletStream {
    public StreamBuffer() {
        this(SegmentBufferStrategy.defaultPooledStrategy(ByteArrayPool.instance()));
    }

    public StreamBuffer(SegmentBufferStrategy<byte[]> strategy) {
        super(strategy);
    }

    @Override
    public void close() {

    }
}
