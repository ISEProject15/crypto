package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

import project.lib.scaffolding.streaming.BufferWriter;
import project.lib.scaffolding.streaming.StreamUtil;

public class SegmentBuffer<T> extends Sequence<T> {
    public SegmentBuffer(SegmentBufferStrategy<T> strategy, Class<T> cls) {
        super(cls);
        if (strategy == null) {
            throw new IllegalArgumentException();
        }
        this.strategy = strategy;
        this.writer = new Writer();
    }

    public final SegmentBufferStrategy<T> strategy;
    private Segment firstSegment;
    private int firstIndex;
    private Segment lastSegment;
    private int lastIndex;
    private int length;

    private final Writer writer;

    public BufferWriter<T> writer() {
        return this.writer;
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public SequenceSegment<T> firstSegment() {
        return this.firstSegment;
    }

    @Override
    public SequenceSegment<T> lastSegment() {
        return this.lastSegment;
    }

    @Override
    public int firstIndex() {
        return this.firstIndex;
    }

    @Override
    public int lastIndex() {
        return this.lastIndex;
    }

    public void write(T source, int offset, int length) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        length = StreamUtil.lenof(length);
        var written = 0;
        final var writer = this.writer;
        while (written < length) {

            final var rest = length - written;
            if (!writer.tryStage(0)) {
                writer.stage(rest);
            }
            final var buffer = writer.stagedBuffer();
            final var stagedLength = writer.stagedLength();
            final var stagedOffset = writer.stagedOffset();
            final var toWrite = Math.min(rest, stagedLength);
            System.arraycopy(source, offset, buffer, stagedOffset, toWrite);
            written += toWrite;
            writer.finish(toWrite);
        }
    }

    public int read(T destination, int offset, int length) {
        if (destination == null || offset < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        if (offset + length > Array.getLength(destination)) {
            throw new IllegalArgumentException();
        }

        final var lastSegment = this.lastSegment;
        final var strategy = this.strategy;
        var written = 0;
        var segment = this.firstSegment;
        var segmentOffset = this.firstIndex;

        while (segment != null && written < length) {
            final var rest = length - written;
            final var segmentLength = segment == lastSegment ? this.lastIndex : segment.length;

            final var toWrite = Math.min(segmentLength - segmentOffset, rest);
            System.arraycopy(segment.buffer, segmentOffset + segment.offset, destination, written + offset, toWrite);

            if (segmentLength <= rest) {
                final var next = segment.next;
                strategy.backSegmentBuffer(segment.buffer);

                segment = next;
                segmentOffset = 0;
            } else {
                segmentOffset += toWrite;
            }

            written += toWrite;
        }

        this.firstSegment = segment;
        this.firstIndex = segmentOffset;
        if (segment == null) {// sequence is empty
            this.lastSegment = null;
        }
        this.length -= written;

        if (this.isEmpty()) {
            return ~written;
        }
        return written;
    }

    public int read(T destination, int offset) {
        return this.read(destination, offset, Array.getLength(destination));
    }

    public int read(T destination) {
        return this.read(destination, 0);
    }

    // discard buffered items
    public void discard(int amount) {
        if (amount <= 0) {
            return;
        }

        final var lastSegment = this.lastSegment;
        final var strategy = this.strategy;
        var discarded = 0;
        var segment = this.firstSegment;
        var offset = this.firstIndex;
        while (segment != null && discarded < amount) {
            final var rest = amount - discarded;
            final var length = segment == lastSegment ? this.lastIndex : segment.length;
            final var toDiscard = Math.min(length, rest);
            if (length <= rest) {
                final var next = segment.next;
                strategy.backSegmentBuffer(segment.buffer);
                segment = next;
                offset = 0;
            } else {
                offset += toDiscard;
            }
            discarded += toDiscard;
        }

        this.firstSegment = segment;
        this.firstIndex = offset;
        if (segment == null) {
            this.lastSegment = null;
        }
        this.length -= discarded;
    }

    private final class Writer implements BufferWriter<T> {
        private T stagedBuffer;
        private int stagedLength;

        private void throwIfAlreadyStaged() {
            if (this.stagedBuffer != null) {
                throw new IllegalStateException("buffer was already staged");
            }
        }

        private void throwIfNotStaged() {
            if (this.stagedBuffer == null) {
                throw new IllegalStateException("buffer is not staged");
            }
        }

        // put segment that is greater than capacity into stage
        public void stage(int capacity) {
            this.throwIfAlreadyStaged();
            final var buffer = strategy.requireSegmentBuffer(capacity);
            this.stagedLength = Array.getLength(buffer);
            this.stagedBuffer = buffer;
        }

        // try put pooled segment that is greater than capacity into stage
        public boolean tryStage(int capacity) {
            this.throwIfAlreadyStaged();
            final var buffer = strategy.tryRequireSegmentBuffer(capacity);
            if (buffer == null) {
                return false;
            }
            this.stagedLength = Array.getLength(buffer);
            this.stagedBuffer = buffer;
            return true;
        }

        @Override
        public void finish(int length) {
            this.throwIfNotStaged();
            length = StreamUtil.lenof(length);
            final var buffer = this.stagedBuffer;
            if (this.stagedLength < length) {
                throw new IllegalStateException();
            }
            if (length == 0) {// if no data written, return staged to pool
                strategy.backSegmentBuffer(this.stagedBuffer);
                this.stagedBuffer = null;
                return;
            }

            final var segment = new Segment(buffer, 0, StreamUtil.lenof(length));

            if (firstSegment == null) {// sequence has no segment
                firstSegment = lastSegment = segment;
                firstIndex = 0;
                lastIndex = length;
            } else {
                assert lastSegment != null : "if sequence is not empty, lastSegment should not null";
                lastSegment.next = segment;
                lastSegment = segment;
                lastIndex = length;
            }

            SegmentBuffer.this.length += length;
        }

        @Override
        public T stagedBuffer() {
            this.throwIfNotStaged();
            return this.stagedBuffer;
        }

        @Override
        public int stagedLength() {
            this.throwIfNotStaged();
            return this.stagedLength;
        }

        @Override
        public int stagedOffset() {
            this.throwIfNotStaged();

            return 0;
        }

    }

    private final class Segment extends SequenceSegment<T> {
        Segment(T source, int offset, int length) {
            super(source);
            this.offset = offset;
            this.length = length;
        }

        Segment next;
        int offset;
        int length;

        @Override
        public Segment next() {
            return this.next;
        }

        @Override
        public int offset() {
            return this.offset;
        }

        @Override
        public int length() {
            return this.length;
        }
    }
}
