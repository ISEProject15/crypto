package project.lib.scaffolding.streaming;

import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.collections.SequenceSegment;

public class SubSequence<T> extends Sequence<T> {
    public SubSequence(Class<T> cls, SequenceSegment<T> firstSegment, int firstIndex, SequenceSegment<T> lastSegment,
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
    public SequenceSegment<T> firstSegment() {
        return this.firstSegment;
    }

    @Override
    public int firstIndex() {
        return this.firstIndex;
    }

    @Override
    public SequenceSegment<T> lastSegment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int lastIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

}
