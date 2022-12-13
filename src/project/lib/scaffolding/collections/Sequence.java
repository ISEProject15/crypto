package project.lib.scaffolding.collections;

import project.lib.scaffolding.streaming.SequenceStreamReader;

public abstract class Sequence<T> {
    protected Sequence(Class<T> bufferClass) {
        if (!bufferClass.isArray()) {
            throw new IllegalArgumentException();
        }
        this.bufferClass = bufferClass;
    }

    public final Class<T> bufferClass;

    public abstract SequenceSegment<T> firstSegment();

    // inclusive
    public abstract int firstIndex();

    public abstract SequenceSegment<T> lastSegment();

    // exclusive
    public abstract int lastIndex();

    public long firstTotalIndex() {
        return this.firstSegment().totalIndex() + this.firstIndex();
    }

    public long lastTotalIndex() {
        return this.lastSegment().totalIndex() + this.lastIndex();
    }

    public Sequence<T> slice(int offset) {
        final var view = new SequenceStreamReader<>(this.bufferClass, this.firstSegment(), this.firstIndex(), this.lastSegment(), this.lastIndex());
        view.advance(offset);
        return view;
    }

    public long length() {
        final var firstSegment = this.firstSegment();
        final var lastSegment = this.lastSegment();
        if (firstSegment == null) {
            return 0;
        }
        return lastSegment.totalIndex() - firstSegment.totalIndex() + (this.lastIndex() - this.firstIndex());
    }

    public boolean isSingleSegment() {
        return this.firstSegment() == this.lastSegment();
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }

    public SequenceIterator<T> iterator() {
        return SequenceIterator.from(this);
    }

    public T toArray() {
        final var totalLength = this.length();
        final var array = ArrayUtil.create(bufferClass, (int) totalLength);
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
