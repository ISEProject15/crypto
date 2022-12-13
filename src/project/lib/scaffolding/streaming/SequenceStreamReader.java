package project.lib.scaffolding.streaming;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.collections.SequenceSegment;

public final class SequenceStreamReader<T> extends Sequence<T> implements StreamReader<T> {
    public static <T> SequenceStreamReader<T> from(Sequence<T> sequence) {
        return new SequenceStreamReader<>(sequence.bufferClass,
                sequence.firstSegment(), sequence.firstIndex(),
                sequence.lastSegment(), sequence.lastIndex());
    }

    public SequenceStreamReader(Class<T> cls, SequenceSegment<T> firstSegment, int firstIndex,
            SequenceSegment<T> lastSegment,
            int lastIndex) {
        super(cls);
        this.firstSegment = firstSegment;
        this.firstIndex = firstIndex;
        this.lastSegment = lastSegment;
        this.lastIndex = lastIndex;
    }

    private SequenceSegment<T> firstSegment;
    private int firstIndex;
    private SequenceSegment<T> lastSegment;
    private int lastIndex;

    @Override
    public void advance(int consumed) {
        if (consumed <= 0) {
            return;
        }
        var firstSegment = this.firstSegment;
        var firstIndex = this.firstIndex;
        final var lastSegment = this.lastSegment;
        final var lastIndex = this.lastIndex;
        if (this.isEmpty()) {
            return;
        }
        while (true) {
            final var isLast = firstSegment == lastSegment;
            final var firstLength = isLast ? lastIndex : firstSegment.length();
            final var rest = firstLength - firstIndex;
            if (rest <= consumed) {
                if (isLast) {
                    firstIndex = lastIndex;
                    break;
                }
                firstSegment = firstSegment.next();
                firstIndex = 0;
                consumed -= rest;
            } else {
                firstIndex += rest;
                break;
            }
        }

        this.firstSegment = firstSegment;
        this.firstIndex = firstIndex;
    }

    @Override
    public SequenceSegment<T> firstSegment() {
        return this.firstSegment;
    }

    @Override
    public int firstIndex() {
        return this.firstIndex;
    }

    @Override
    public SequenceSegment<T> lastSegment() {
        return this.lastSegment;
    }

    @Override
    public int lastIndex() {
        return this.lastIndex;
    }

    @Override
    public Sequence<T> read(int least) throws IOException {
        return this;
    }

    @Override
    public boolean completed() {
        return true;
    }
}
