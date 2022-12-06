package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

import project.lib.StreamUtil;

public class SegmentBuffer<T> extends Sequence<T> {
    public SegmentBuffer(SegmentBufferStrategy strategy, Class<T> cls) {
        super(cls);
        if (strategy == null) {
            throw new IllegalArgumentException();
        }
        this.strategy = strategy;
        this.writer = new Writer();
    }

    public SegmentBuffer(Class<T> cls) {
        this(SegmentBufferStrategy.defaultStrategy, cls);
    }

    public final SegmentBufferStrategy strategy;
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
            final var w = writer.staged.write(source, written + offset, rest);
            written += w;
            writer.finish(w);
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
        var written = 0;
        var segment = this.firstSegment;
        var segmentOffset = this.firstIndex;
        final var writer = this.writer;

        while (segment != null && written < length) {
            final var rest = length - written;
            final var segmentLength = segment == lastSegment ? this.lastIndex : segment.length;

            final var toWrite = Math.min(segmentLength - segmentOffset, rest);
            System.arraycopy(segment.buffer, segmentOffset + segment.offset, destination, written + offset, toWrite);

            if (segmentLength <= rest) {
                final var next = segment.next;
                writer.pushPool(segment);
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
        var discarded = 0;
        var segment = this.firstSegment;
        var offset = this.firstIndex;
        final var writer = this.writer;
        while (segment != null && discarded < amount) {
            final var rest = amount - discarded;
            final var length = segment == lastSegment ? this.lastIndex : segment.length;
            final var toDiscard = Math.min(length, rest);
            if (length <= rest) {
                final var next = segment.next;
                writer.pushPool(segment);
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

    private Class<?> elementCls() {
        return this.bufferClass.componentType();
    }

    private final class Writer implements BufferWriter<T> {
        private Segment pool;
        private Segment staged;

        public void pushPool(Segment segment) {
            segment.next = this.pool;
            segment.clear();
            this.pool = segment;
        }

        // put segment that is greater than capacity into stage
        public void stage(int capacity) {
            capacity = Math.max(capacity, strategy.nextSegmentSize(0));
            if (!this.tryStage(capacity)) {
                final var size = strategy.nextSegmentSize(capacity);
                final var staged = new Segment(size);
                this.staged = staged;
            }
        }

        // try put pooled segment that is greater than capacity into stage
        public boolean tryStage(int capacity) {
            if (this.staged != null) {
                throw new IllegalStateException();
            }
            Segment prev = null;
            var segment = this.pool;
            while (segment != null) {
                if (segment.length >= capacity) {
                    break;
                }
                prev = segment;
                segment = segment.next;
            }

            if (segment == null) {
                return false;
            }

            if (prev == null) {
                this.pool = segment.next;
            } else {
                prev.next = segment.next;
            }
            this.staged = segment;
            segment.next = null;
            return true;
        }

        @Override
        public void finish(int length) {
            if (length < 0) {
                throw new IllegalArgumentException();
            }
            final var staged = this.staged;
            if (staged == null) {
                throw new IllegalStateException();
            }
            this.staged = null;
            if (staged.length < length) {
                throw new IllegalStateException();
            }
            if (length == 0) {// if no data written, return staged to pool
                staged.next = this.pool;
                this.pool = staged;
                return;
            }
            final var rest = staged.length - length;
            staged.length = length;
            if (rest > 0) {// segment has extra space
                final var p = new Segment(staged.buffer, length, rest);
                p.next = this.pool;
                this.pool = p;
            }

            if (firstSegment == null) {// sequence has no segment
                firstSegment = lastSegment = staged;
                firstIndex = 0;
                lastIndex = length;
            } else {
                lastSegment.next = staged;
                lastSegment = staged;
                lastIndex = length;
            }

            SegmentBuffer.this.length += length;
        }

        @Override
        public T stagedBuffer() {
            if (this.staged == null) {
                throw new IllegalStateException();
            }
            return this.staged.buffer;
        }

        @Override
        public int stagedLength() {
            if (this.staged == null) {
                throw new IllegalStateException();
            }
            return this.staged.length;
        }

        @Override
        public int stagedOffset() {
            if (this.staged == null) {
                throw new IllegalStateException();
            }
            return this.staged.offset;
        }

    }

    private final class Segment extends SequenceSegment<T> {
        @SuppressWarnings("unchecked")
        Segment(int capacity) {
            super((T) Array.newInstance(elementCls(), capacity));
            this.length = capacity;
        }

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

        public void clear() {
            ArrayUtil.clear(this.buffer, this.offset, this.length + this.offset);
        }

        public int write(T source, int offset, int length) {
            length = StreamUtil.lenof(length);
            final var sourceLength = Array.getLength(source);
            if (length + offset > sourceLength) {
                throw new IllegalArgumentException();
            }
            final var len = Math.min(length, this.length);
            System.arraycopy(source, offset, this.buffer, this.offset, len);
            return len;
        }
    }
}
