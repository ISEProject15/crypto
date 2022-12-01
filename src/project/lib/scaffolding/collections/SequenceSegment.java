package project.lib.scaffolding.collections;

import java.lang.reflect.Array;

import project.lib.StreamUtil;

public abstract class SequenceSegment<T> {
    protected SequenceSegment(T buffer) {
        this.buffer = buffer;
    }

    public final T buffer;

    public abstract int length();

    public abstract int offset();

    public abstract SequenceSegment<T> next();

    public int write(T source, int offset, int length) {
        length = StreamUtil.lenof(length);
        final var sourceLength = Array.getLength(source);
        if (length + offset > sourceLength) {
            throw new IllegalArgumentException();
        }
        final var len = Math.min(length, this.length());
        System.arraycopy(source, offset, this.buffer, this.offset(), len);
        return len;
    }
}
