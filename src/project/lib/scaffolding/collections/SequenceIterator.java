package project.lib.scaffolding.collections;

public final class SequenceIterator<T> {
    public static <T> SequenceIterator<T> from(Sequence<T> sequence) {
        return new SequenceIterator<>(sequence);
    }

    private SequenceIterator(Sequence<T> sequence) {
        this.firstSegment = sequence.firstSegment();
        this.lastSegment = sequence.lastSegment();
        this.firstIndex = sequence.firstIndex();
        this.lastIndex = sequence.lastIndex();
    }

    private final SequenceSegment<T> firstSegment;
    private final SequenceSegment<T> lastSegment;
    private final int firstIndex;
    private final int lastIndex;

    private SequenceSegment<T> segment;
    private int offset;
    private int length;

    public T currentBuffer() {
        if (this.segment == null) {
            throw new IllegalStateException();
        }
        return segment.buffer;
    }

    public int currentOffset() {
        if (this.segment == null) {
            throw new IllegalStateException();
        }
        return this.offset;
    }

    public int currentLength() {
        if (this.segment == null) {
            throw new IllegalStateException();
        }

        return this.length;
    }

    public boolean hasNext() {
        if (this.segment == null) {// first call
            return this.firstSegment != null;
        }
        return this.segment.next() != null;
    }

    public void move() {
        final var last = this.lastSegment;
        if (this.segment == null) {// first call
            final var first = this.firstSegment;
            if (first == null) {
                throw new IllegalStateException();
            }
            this.segment = first;
            this.offset = firstIndex + first.offset();
            this.length = first == last ? this.lastIndex : first.length();
            return;
        }

        final var next = this.segment.next();

        if (next == null) {
            throw new IllegalStateException();
        }
        this.segment = next;
        // next is absolutely not first segment, so we not have to check first index
        this.offset = next.offset();
        this.length = next == last ? this.lastIndex : next.length();
    }

}
