package project.lib.scaffolding.collections;

import java.io.Closeable;
import java.lang.reflect.Array;

import project.lib.scaffolding.streaming.BufferWriter;
import project.lib.scaffolding.streaming.StreamUtil;

public class SegmentBuffer<T> extends Sequence<T> implements Closeable {
    public SegmentBuffer(SegmentBufferStrategy<T> strategy) {
        super(strategy.bufferClass());
        this.strategy = strategy;
        this.writer = new Writer();
    }

    public final SegmentBufferStrategy<T> strategy;
    private Segment firstSegment;
    private int firstIndex;
    private Segment lastSegment;
    private int lastIndex;

    private final Writer writer;

    public BufferWriter<T> writer() {
        return this.writer;
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

    @Override
    public void close() {
        final var strategy = this.strategy;
        var segment = this.firstSegment;
        while (segment != null) {
            strategy.backSegmentBuffer(segment.buffer);
        }
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
        var firstSegment = this.firstSegment;
        var firstIndex = this.firstIndex;
        if (firstSegment == null) {
            return -1;
        }

        var written = 0;
        while (true) {
            final var rest = length - written;
            final var isLast = firstSegment == lastSegment;
            final var firstLength = isLast ? this.lastIndex : firstSegment.length;
            final var firstRest = firstLength - firstIndex;
            final var bufferOffset = firstIndex + firstSegment.offset;
            final var dstOffset = written + offset;

            if (firstRest <= rest) {
                System.arraycopy(firstSegment.buffer, bufferOffset, destination, dstOffset, firstRest);
                written += firstRest;
                if (isLast) {
                    firstIndex = lastIndex;
                    break;
                }
                strategy.backSegmentBuffer(firstSegment.buffer);
                firstSegment = firstSegment.next;
                firstIndex = 0;
            } else {
                System.arraycopy(firstSegment.buffer, bufferOffset, destination, dstOffset, rest);
                written += rest;
                firstIndex += rest;
                break;
            }

        }

        this.firstSegment = firstSegment;
        this.firstIndex = firstIndex;
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

        final var strategy = this.strategy;
        final var lastSegment = this.lastSegment;
        final var lastIndex = this.lastIndex;
        var firstSegment = this.firstSegment;
        var firstIndex = this.firstIndex;

        if (firstSegment == null) {
            return;
        }

        while (true) {
            final var isLast = firstSegment == lastSegment;
            final var firstLength = isLast ? lastIndex : firstSegment.length;
            final var rest = firstLength - firstIndex;
            if (rest <= amount) {
                if (isLast) {
                    firstIndex = lastIndex;
                    break;
                }
                strategy.backSegmentBuffer(firstSegment.buffer);
                firstSegment = firstSegment.next;
                firstIndex = 0;
                amount -= rest;
            } else {
                firstIndex += amount;
                break;
            }
        }

        this.firstSegment = firstSegment;
        this.firstIndex = firstIndex;
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
            @SuppressWarnings("resource")
            final var sequence = SegmentBuffer.this;
            length = StreamUtil.lenof(length);
            final var buffer = this.stagedBuffer;
            this.stagedBuffer = null;
            if (this.stagedLength < length) {
                throw new IllegalStateException();
            }
            if (length == 0) {// if no data written, return staged to pool
                strategy.backSegmentBuffer(this.stagedBuffer);
                return;
            }

            final var segment = new Segment(buffer, 0, StreamUtil.lenof(length), 0);

            if (sequence.firstSegment == null) {// sequence has no segment
                sequence.firstSegment = sequence.lastSegment = segment;
                sequence.firstIndex = 0;
                sequence.lastIndex = length;
            } else {
                assert sequence.lastSegment != null : "if sequence is not empty, lastSegment should not null";
                final var totalIndex = sequence.lastSegment.successorTotalIndex();
                sequence.lastSegment.next = segment;
                sequence.lastSegment = segment;
                sequence.lastIndex = length;
                segment.totalIndex = totalIndex;
            }
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
        Segment(T source, int offset, int length, long totalIndex) {
            super(source);
            this.offset = offset;
            this.length = length;
            this.totalIndex = totalIndex;
        }

        Segment next;
        int offset;
        int length;
        long totalIndex;

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

        @Override
        public long totalIndex() {
            return this.totalIndex;
        }

        public long successorTotalIndex() {
            return this.totalIndex() + this.length;
        }
    }
}
