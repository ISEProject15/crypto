package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.collections.SequenceSegment;

public class ArrayStreamReader<T> implements StreamReader<T> {
    @SuppressWarnings("unchecked")
    public ArrayStreamReader(T array, int offset, int length) {
        this.sequence = new SingleSegmentSequence<>(
                (Class<T>) array.getClass(),
                new ArraySequenceSegment<T>(array, offset, length), 0, 0);
    }

    private final SingleSegmentSequence<T> sequence;

    @Override
    public void advance(int consumed) throws IOException {
        if (consumed < 0) {
            throw new IllegalArgumentException();
        }
        final var nextFirst = sequence.firstIndex + consumed;
        sequence.firstIndex += Math.min(nextFirst, sequence.lastIndex);
    }

    @Override
    public Sequence<T> read(int least) throws IOException {
        return this.sequence;
    }

    @Override
    public boolean completed() {
        return this.sequence.isEmpty();
    }

    private static class ArraySequenceSegment<T> extends SequenceSegment<T> {
        protected ArraySequenceSegment(T buffer, int offset, int length) {
            super(buffer);
            this.offset = offset;
            this.length = length;
        }

        private final int offset;
        private final int length;

        @Override
        public int length() {
            return this.length;
        }

        @Override
        public int offset() {
            return this.offset;
        }

        @Override
        public SequenceSegment<T> next() {
            return null;
        }
    }

    private static class SingleSegmentSequence<T> extends Sequence<T> {
        SingleSegmentSequence(Class<T> cls, SequenceSegment<T> segment, int firstIndex, int lastIndex) {
            super(cls);
            this.segment = segment;
            this.firstIndex = firstIndex;
            this.lastIndex = lastIndex;
        }

        private final SequenceSegment<T> segment;
        public int firstIndex;
        public int lastIndex;

        @Override
        public SequenceSegment<T> firstSegment() {
            return this.segment;
        }

        @Override
        public int firstIndex() {
            return this.firstIndex;
        }

        @Override
        public SequenceSegment<T> lastSegment() {
            return this.segment;
        }

        @Override
        public int lastIndex() {
            return this.lastIndex;
        }

        @Override
        public int length() {
            return this.lastIndex - this.firstIndex;
        }

    }
}
