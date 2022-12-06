package project.lib.scaffolding.collections;

public abstract class SequenceSegment<T> {
    protected SequenceSegment(T buffer) {
        this.buffer = buffer;
    }

    public final T buffer;

    public abstract int length();

    public abstract int offset();

    public abstract SequenceSegment<T> next();
}
