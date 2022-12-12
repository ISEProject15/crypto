package project.lib.scaffolding.streaming;

import java.lang.reflect.Array;

import project.lib.scaffolding.ArrayPool;

public abstract class PooledBufferWriterBase<T> implements BufferWriter<T> {
    protected PooledBufferWriterBase(ArrayPool<T> pool) {
        this.pool = pool;
    }

    private final ArrayPool<T> pool;
    private T stagedBuffer;
    private int stagedLength;

    private final void throwIfAlreadyStaged() {
        if (this.stagedBuffer != null) {
            throw new IllegalStateException("buffer was already staged");
        }
    }

    private final void throwIfNotStaged() {
        if (this.stagedBuffer == null) {
            throw new IllegalStateException("buffer is not staged");
        }
    }

    @Override
    public final void stage(int minimumLength) {
        this.throwIfAlreadyStaged();
        final var buffer = pool.rent(minimumLength);
        this.stagedBuffer = buffer;
        this.stagedLength = Array.getLength(buffer);
    }

    @Override
    public final boolean tryStage(int minimumLength) {
        this.throwIfAlreadyStaged();
        final var buffer = pool.tryRent(minimumLength);
        if (buffer == null) {
            return false;
        }
        this.stagedBuffer = buffer;
        this.stagedLength = Array.getLength(buffer);
        return true;
    }

    @Override
    public final T stagedBuffer() {
        this.throwIfNotStaged();
        return this.stagedBuffer;
    }

    @Override
    public final int stagedOffset() {
        this.throwIfNotStaged();
        return 0;
    }

    @Override
    public final int stagedLength() {
        this.throwIfNotStaged();
        return this.stagedLength;
    }

    @Override
    public final void finish(int written) {
        this.throwIfNotStaged();
        if (this.stagedLength < StreamUtil.lenof(written)) {
            throw new IllegalArgumentException("written count should less than or equal to staged buffer length");
        }
        final var buffer = this.stagedBuffer;
        this.stagedBuffer = null;
        this.onFinish(buffer, 0, StreamUtil.lenof(written));
    }

    protected abstract void onFinish(T buffer, int offset, int length);
}
