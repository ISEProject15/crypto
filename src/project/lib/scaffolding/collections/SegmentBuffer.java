package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

import project.lib.StreamUtil;

//FIXME: length mismatch 
public class SegmentBuffer<T> extends Sequence<T> {
    public SegmentBuffer(SegmentBufferStrategy strategy, Class<T> cls) {
        if (!cls.isArray()) {
            throw new IllegalArgumentException();
        }
        if (strategy == null) {
            throw new IllegalArgumentException();
        }
        this.bufferCls = cls;
        this.strategy = strategy;
    }

    public SegmentBuffer(Class<T> cls) {
        this(SegmentBufferStrategy.defaultStrategy, cls);
    }

    private final Class<T> bufferCls;
    public final SegmentBufferStrategy strategy;
    private Segment firstSegment;
    private int firstOffset;
    private Segment lastSegment;
    private int length;
    private Segment pool;
    private Segment rental;

    public int length() {
        return this.length;
    }

    public boolean isEmpty() {
        return this.length <= 0;
    }

    // put segment that is greater than capacity into stage
    public SequenceSegment<T> stage(int capacity) {
        capacity = Math.max(capacity, this.strategy.nextSegmentSize(0));
        final var segment = tryStage(capacity);
        if (segment == null) {
            final var size = this.strategy.nextSegmentSize(capacity);
            final var rental = new Segment(size);
            this.rental = rental;
            return rental;
        }
        return segment;
    }

    // try put pooled segment that is greater than capacity into stage
    public SequenceSegment<T> tryStage(int capacity) {
        if (this.rental != null) {
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
            return null;
        }

        if (prev == null) {
            this.pool = segment.next;
        } else {
            prev.next = segment.next;
        }
        this.rental = segment;
        segment.next = null;
        return segment;
    }

    // notify how many items written to buffer and clear stage
    public void notifyWritten(int length) {
        if (length < 0) {
            throw new IllegalArgumentException();
        }
        final var rental = this.rental;
        if (rental == null) {
            throw new IllegalStateException();
        }
        if (rental.length < length) {
            throw new IllegalStateException();
        }
        this.rental = null;
        if (length == 0) {// if no data written, return rental to pool
            rental.next = this.pool;
            this.pool = rental;
            return;
        }
        final var minSegmentSize = this.strategy.nextSegmentSize(0);
        final var rest = rental.length - length;
        rental.length = length;
        if (rest >= minSegmentSize) {// segment has enough extra space
            final var p = new Segment(rental.buffer, length, rest);
            p.next = this.pool;
            this.pool = p;
        }

        if (this.lastSegment == null) {// sequence has no segment
            this.firstSegment = this.lastSegment = rental;
            this.firstOffset = 0;
        } else {
            this.lastSegment.next = rental;
            this.lastSegment = rental;
        }

        this.length += length;
    }

    public void write(T source, int offset, int length) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        length = StreamUtil.lenof(length);
        var written = 0;

        while (written < length) {
            var newSegment = this.tryStage(0);
            final var rest = length - written;
            if (newSegment == null) {
                newSegment = this.stage(rest);
            }
            final var w = newSegment.write(source, written + offset, rest);
            written += w;
            this.notifyWritten(w);
        }
    }

    public int read(T destination, int offset, int length) {
        if (destination == null || offset < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        if (offset + length > Array.getLength(destination)) {
            throw new IllegalArgumentException();
        }

        var written = 0;
        var segment = this.firstSegment;
        var segmentOffset = this.firstOffset;
        var pool = this.pool;
        System.out.println(segment);
        while (segment != null && written < length) {
            final var rest = length - written;
            final var segmentLength = segment.length - segmentOffset;

            final var toWrite = Math.min(segmentLength, rest);
            System.arraycopy(segment.buffer, segmentOffset, destination, written + offset, toWrite);

            if (segmentLength <= rest) {
                final var next = segment.next;
                segment.next = pool;
                pool = segment;
                segment = next;
                segmentOffset = next != null ? next.offset : 0;
            } else {
                segmentOffset += toWrite;
            }

            written += toWrite;
        }

        this.firstSegment = segment;
        this.firstOffset = segmentOffset;
        this.pool = pool;
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
        var written = 0;
        var segment = this.firstSegment;
        var offset = this.firstOffset;
        var pool = this.pool;
        while (segment != null && written < amount) {
            final var rest = amount - written;
            final var length = segment.length;
            final var discarded = Math.min(length, rest);
            if (length <= rest) {
                final var next = segment.next;
                segment.next = pool;
                pool = segment;
                segment = next;
                offset = next != null ? next.offset : 0;
            } else {
                offset += discarded;
            }
            written += discarded;
        }

        this.firstSegment = segment;
        this.firstOffset = offset;
        this.pool = pool;
        this.length -= written;
    }

    public SequenceSegment<T> first() {
        return this.firstSegment;
    }

    public SequenceSegment<T> last() {
        return this.lastSegment;
    }

    @SuppressWarnings("unchecked")
    public T toArray() {
        final var array = (T) Array.newInstance(elementCls(), this.length);
        var written = 0;
        var segment = this.firstSegment;
        while (written < length) {
            final var len = segment.length();
            System.arraycopy(segment.buffer, segment.offset(), array, written, len);
            written += len;
            segment = segment.next;
        }
        return array;
    }

    private Class<?> elementCls() {
        return this.bufferCls.componentType();
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
            final var affiliation = SegmentBuffer.this;
            if (this == affiliation.firstSegment) {// if this is rental segment, always not equal to firstSegment
                return affiliation.firstOffset + this.offset;
            }
            return 0;
        }

        @Override
        public int length() {
            final var affiliation = SegmentBuffer.this;
            if (this == affiliation.firstSegment) {// if this is rental segment, always not equal to firstSegment
                return this.length - affiliation.firstOffset;
            }
            return this.length;
        }
    }
}
