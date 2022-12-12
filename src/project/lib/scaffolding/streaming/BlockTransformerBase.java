package project.lib.scaffolding.streaming;

import java.lang.reflect.Array;

import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.Sequence;

public abstract class BlockTransformerBase<T> implements Transformer<T> {
    @SuppressWarnings("unchecked")
    protected BlockTransformerBase(int blockLength, ArrayPool<T> pool, SegmentBufferStrategy<T> strategy) {
        final var block = (T) Array.newInstance(pool.arrayClass.componentType(), blockLength);
        final var listener = BlockBufferWriterProxy.wrap(block, this::transform);
        this.blockLength = blockLength;
        this.writer = new DefaultPooledBufferWriter<>(pool, listener);
        this.buffer = new SegmentBuffer<>(strategy);
    }

    protected BlockTransformerBase(int blockLength, ArrayPool<T> pool) {
        this(blockLength, pool, SegmentBufferStrategy.defaultPooledStrategy(pool));
    }

    protected final int blockLength;
    protected final BufferWriter<T> writer;
    protected final SegmentBuffer<T> buffer;
    private boolean completed;

    protected abstract void transform(T buffer, int offset, int length);

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

}
