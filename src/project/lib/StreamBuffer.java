package project.lib;

import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.SequenceSegment;

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

    public StreamBuffer.Iterator iterator() {
        return new Iterator(this.first());
    }

    public static class Iterator {
        private Iterator(SequenceSegment<byte[]> segment) {
            this.segment = segment;
            this.index = segment.offset() - 1;
        }

        private SequenceSegment<byte[]> segment;
        private int index;

        public boolean hasNext() {
            if (segment == null) {
                return false;
            }
            if (segment.next() != null) {
                return true;
            }
            return this.index < segment.length();
        }

        public byte next() {
            this.index++;
            final var segment = this.segment;
            if (this.index >= segment.length()) {
                final var next = segment.next();
                this.segment = next;
                this.index = next.offset() - 1;
                return this.next();
            }
            return segment.buffer[this.index];
        }

    }
}
