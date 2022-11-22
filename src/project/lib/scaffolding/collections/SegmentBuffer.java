package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

import project.lib.StreamUtil;

public class SegmentBuffer<T> {
    public SegmentBuffer(SegmentBufferStrategy strategy, Class<T> cls) {
        if (!cls.isArray()) {
            throw new IllegalArgumentException();
        }
        this.bufferCls = cls;
        this.strategy = strategy;
    }

    public SegmentBuffer(Class<T> cls) {
        this(SegmentBufferStrategy.doublingMin16Strategy, cls);
    }

    private final Class<T> bufferCls;
    private final SegmentBufferStrategy strategy;
    private Segment firstSegment;
    private int firstOffset;
    private Segment lastSegment;
    private int lastLength;
    private int length;
    private Segment pool;

    public int length() {
        return this.length;
    }

    public boolean isEmpty() {
        return this.length() <= 0;
    }

    public Segment rent(int capacity) {
        final var segment = tryRent(capacity);
        if (segment == null) {
            return new Segment(this.nextSegmentSize(capacity));
        }
        return segment;
    }

    public Segment tryRent(int capacity) {
        Segment prev = null;
        var segment = this.pool;
        while (segment != null) {
            if (segment.capacity >= capacity) {
                break;
            }
            prev = segment;
        }

        if (segment == null) {
            return null;
        }

        if (prev == null) {
            this.pool = segment.next;
        } else {
            prev.next = segment.next;
        }

        return segment;
    }

    public void push(Segment segment, int length) {
        if (this.lastSegment == null) {
            this.firstSegment = this.lastSegment = segment;
            this.firstOffset = 0;
        } else {
            this.lastSegment.next = segment;
            this.lastSegment = segment;
        }
        this.lastLength = length;

        this.length += length;
    }

    public void write(T source, int length) {
        length = StreamUtil.lenof(length);
        var segment = this.lastSegment;
        var written = segment.write(source, this.lastLength, length);
        this.lastLength += written;

        while (written < length) {
            var newSegment = this.tryRent(0);
            final var rest = length - written;
            if (newSegment == null) {
                newSegment = this.rent(rest);
            }
            final var w = newSegment.write(source, written, rest);
            written += w;
            this.push(newSegment, w);
        }
        this.length += length;
    }

    public int read(T destination) {
        final var destinationLength = Array.getLength(destination);
        final var lastSegment = this.lastSegment;
        var written = 0;
        var first = this.firstSegment;
        var offset = this.firstOffset;
        var pool = this.pool;
        while (first != null && written < destinationLength) {
            final var rest = destinationLength - written;
            final var length = (first == lastSegment ? this.lastLength : first.capacity) - offset;
            final var toWrite = Math.min(length, rest);
            System.arraycopy(first.buffer, offset, destination, written, toWrite);

            if (length <= rest) {
                offset = 0;
                final var next = first.next;
                first.next = pool;
                pool = first;
                first = next;
            } else {
                offset += toWrite;
            }

            written += toWrite;
        }

        this.firstSegment = first;
        this.firstOffset = offset;
        this.pool = pool;
        this.length -= written;

        if (this.isEmpty()) {
            return ~written;
        }
        return written;
    }

    private int nextSegmentSize(int capacity) {
        final var last = this.lastSegment;
        final var size = this.strategy.nextSegmentSize(this.length(), last == null ? 0 : last.capacity);
        return Math.max(capacity, size);
    }

    private Class<?> elementCls() {
        return this.bufferCls.componentType();
    }

    @SuppressWarnings("unchecked")
    public T toArray() {
        final var array = (T) Array.newInstance(elementCls(), this.length);
        final var lastSegment = this.lastSegment;
        var written = 0;
        var segment = this.firstSegment;
        var offset = this.firstOffset;
        while (written < length) {
            final var len = segment == lastSegment ? this.lastLength : segment.capacity;
            System.arraycopy(segment.buffer, offset, array, written, len);
            written += len;
            segment = segment.next;
            offset = 0;
        }
        return array;
    }

    public class Segment {
        @SuppressWarnings("unchecked")
        Segment(int capacity) {
            this.buffer = (T) Array.newInstance(elementCls(), capacity);
            this.capacity = capacity;
        }

        Segment(T source, int length) {
            final T clone = this.cloneBuffer(source);
            this.capacity = Array.getLength(clone);
            this.buffer = clone;
        }

        public final int capacity;
        public final T buffer;
        Segment next;

        @SuppressWarnings("unchecked")
        private T cloneBuffer(T src) {
            try {
                return (T) bufferCls.getMethod("clone").invoke(src);
            } catch (Exception e) {
                throw new UnsupportedOperationException();
            }
        }

        public int write(T source, int offset, int length) {
            length = StreamUtil.lenof(length);
            final var sourceLength = Array.getLength(source);
            if (length + offset > sourceLength) {
                throw new IllegalArgumentException();
            }
            final var len = Math.min(length, this.capacity);
            System.arraycopy(source, offset, this.buffer, 0, len);
            return len;
        }
    }
}
