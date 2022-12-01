package project.lib.scaffolding.collections;

public abstract class Sequence<T> {
    public abstract SequenceSegment<T> first();

    public abstract SequenceSegment<T> last();
}
