package project.lib;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class StreamBuffer implements Iterable<Byte>, InletStream, OutletStream {
    public StreamBuffer(int minimumBufferSize) {
        if (minimumBufferSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.minimumBufferSize = minimumBufferSize;
    }

    public StreamBuffer() {
        this(1024);
    }

    private final int minimumBufferSize;
    private Segment firstSegment;
    private int firstOffset;
    private Segment lastSegment;
    private int length;
    private Segment pool;

    public int length() {
        return this.length;
    }

    public boolean isEmpty() {
        return this.length() <= 0;
    }

    public Segment rent(int capacity) {
        Segment prev = null;
        var segment = this.pool;
        while (segment != null) {
            if (segment.buffer.length >= capacity) {
                break;
            }
            prev = segment;
        }

        if (segment == null) {
            return new Segment(Math.max(capacity, minimumBufferSize));
        }

        if (prev == null) {
            this.pool = segment.next;
        } else {
            prev.next = segment.next;
        }

        return segment;
    }

    public void push(Segment segment) {
        if (this.lastSegment == null) {
            this.firstSegment = this.lastSegment = segment;
            this.firstOffset = 0;
        } else {
            this.lastSegment.next = segment;
            this.lastSegment = segment;
        }

        this.length += segment.length();
    }

    public void write(byte[] source, int length) {
        final var len = StreamUtil.lenof(length);
        final var segment = this.rent(len);
        segment.write(source, len);
        this.push(segment);
    }

    public int read(byte[] destination) {
        var written = 0;
        var first = this.firstSegment;
        var offset = this.firstOffset;
        var pool = this.pool;
        while (first != null && written < destination.length) {
            final var rest = destination.length - written;
            final var length = first.length() - offset;
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

    @Override
    public void close() throws IOException {

    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            byte[] array;
            int length;
            int index;
            Segment next;

            @Override
            public boolean hasNext() {
                return this.next != null || this.index < this.length;
            }

            @Override
            public Byte next() {
                if (this.index >= this.length) {
                    final var next = this.next;
                    this.index = 0;
                    this.length = next.length();
                    this.next = next;
                    return this.next();
                }

                final var result = this.array[this.index];
                this.index++;
                return result;
            }

        };
    }

    public static class Segment {
        Segment(int capacity) {
            this.buffer = new byte[capacity];
        }

        Segment(byte[] source, int length) {
            final var clone = Arrays.copyOf(source, length);
            this.buffer = clone;
            this.length = length;
        }

        public final byte[] buffer;
        private int length;
        Segment next;

        public void write(byte[] source, int length) {
            if (length > this.capacity()) {
                throw new IllegalArgumentException();
            }
            if (length > source.length) {
                throw new IllegalArgumentException();
            }

            System.arraycopy(source, 0, this.buffer, 0, length);
            this.length = length;
        }

        public int capacity() {
            return this.buffer.length;
        }

        public int length() {
            return this.length;
        }

        public void length(int length) {
            if (length > this.capacity()) {
                throw new IllegalArgumentException();
            }
            this.length = length;
        }
    }
}
