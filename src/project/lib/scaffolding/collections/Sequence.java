package project.lib.scaffolding.collections;

public abstract class Sequence<T> {
    public abstract SequenceSegment<T> first();

    public abstract int firstIndex();

    public abstract SequenceSegment<T> last();

    public abstract int lastIndex();

    public abstract int length();

    public boolean isSingleSegment() {
        return this.first() == this.last();
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }
}
