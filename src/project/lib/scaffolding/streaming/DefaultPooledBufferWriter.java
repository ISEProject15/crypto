package project.lib.scaffolding.streaming;

import project.lib.scaffolding.ArrayPool;

public final class DefaultPooledBufferWriter<T> extends PooledBufferWriterBase<T> {
    public DefaultPooledBufferWriter(ArrayPool<T> pool, BufferWriterListener<T> callback) {
        super(pool);
        this.callback = callback;
    }

    private final BufferWriterListener<T> callback;

    @Override
    protected void onFinish(T buffer, int length) {
        this.callback.finished(buffer, 0, length);
    }
}
