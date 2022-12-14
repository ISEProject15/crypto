package project.lib.scaffolding.streaming;

import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.Sequence;

public abstract class TransformerBase<T> implements Transformer<T> {
    protected TransformerBase(ArrayPool<T> pool, SegmentBufferStrategy<T> strategy) {
        final var subject = new BufferWriterSubject<T>();
        this.subject = subject;
        this.writer = new DefaultPooledBufferWriter<>(pool, subject);
        this.buffer = new SegmentBuffer<>(strategy);
    }

    protected TransformerBase(ArrayPool<T> pool) {
        this(pool, SegmentBufferStrategy.defaultPooledStrategy(pool));
    }

    public final BufferWriterSubject<T> subject;
    protected final BufferWriter<T> writer;
    protected final SegmentBuffer<T> buffer;
    private boolean completed;

    protected final void markCompleted() {
        this.completed = true;
    }

    @Override
    public final void advance(int consumed) {
        this.buffer.discard(consumed);
    }

    @Override
    public final boolean completed() {
        return this.completed;
    }

    @Override
    public final Sequence<T> read() {
        return this.buffer;
    }

    @Override
    public final BufferWriter<T> writer() {
        return this.writer;
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}
