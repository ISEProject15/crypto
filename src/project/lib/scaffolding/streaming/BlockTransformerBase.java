package project.lib.scaffolding.streaming;

import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.SegmentBufferStrategy;

public abstract class BlockTransformerBase<T> extends TransformerBase<T> {
    protected BlockTransformerBase(int blockLength, ArrayPool<T> pool, SegmentBufferStrategy<T> strategy) {
        super(pool, strategy);
        final var block = ArrayUtil.create(pool.arrayClass, blockLength);
        this.proxy = BlockBufferWriterListenerProxy.wrap(block, this::wrapper);
        this.blockLength = blockLength;
    }

    protected BlockTransformerBase(int blockLength, ArrayPool<T> pool) {
        this(blockLength, pool, SegmentBufferStrategy.defaultPooledStrategy(pool));
    }

    protected final int blockLength;
    private final BlockBufferWriterListenerProxy<T> proxy;

    @Override
    protected final void transform(T buffer, int offset, int length) {
        this.proxy.finished(buffer, offset, length);
    }

    private final void wrapper(T buffer, int offset, int length) {
        this.transform(buffer, offset, StreamUtil.isLast(length));
    }

    protected abstract void transform(T buffer, int offset, boolean isLast);

}
