package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

public abstract class Sequence<T> {
    protected Sequence(Class<T> bufferClass) {
        if (!bufferClass.isArray()) {
            throw new IllegalArgumentException();
        }
        this.bufferClass = bufferClass;
    }

    public final Class<T> bufferClass;

    public abstract SequenceSegment<T> firstSegment();

    public abstract int firstIndex();

    public abstract SequenceSegment<T> lastSegment();

    public abstract int lastIndex();

    public abstract int length();

    public boolean isSingleSegment() {
        return this.firstSegment() == this.lastSegment();
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }

    public SequenceIterator<T> iterator() {
        return SequenceIterator.from(this);
    }

    @SuppressWarnings("unchecked")
    public T toArray() {
        final var totalLength = this.length();
        final var array = (T) Array.newInstance(bufferClass.componentType(), totalLength);
        final var lastSegment = this.lastSegment();

        var segment = this.firstSegment();
        var offset = this.firstIndex();
        var written = 0;
        while (written < totalLength) {
            final var length = segment == lastSegment ? this.lastIndex() : segment.length();
            offset += segment.offset();
            System.arraycopy(segment.buffer, offset, array, written, length);

            segment = segment.next();
            offset = 0;
            written += length;
        }

        return array;
    }
}
