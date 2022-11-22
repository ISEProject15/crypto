package project.lib;

import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;

public class StreamBuffer extends SegmentBuffer<byte[]> implements InletStream, OutletStream {
    public StreamBuffer() {
        super(SegmentBufferStrategy.doublingMin16Strategy, byte[].class);
    }

    @Override
    public void close() {

    }
}
