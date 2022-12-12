package project.lib.scaffolding.streaming;

import java.lang.reflect.Array;

import project.lib.scaffolding.ArrayPool;

public class DefaultPooledBufferWriter<T> implements BufferWriter<T> {
    public DefaultPooledBufferWriter(ArrayPool<T> pool, BufferWriterFinishedCallback<T> callback) {
        this.pool = pool;
        this.callback = callback;
    }

    private final ArrayPool<T> pool;
    private final BufferWriterFinishedCallback<T> callback;
    private T stagedBuffer;
    private int stagedLength;

    private void throwIfAlreadyStaged() {
        if (this.stagedBuffer != null) {
            throw new IllegalStateException("buffer was already staged");
        }
    }

    private void throwIfNotStaged() {
        if (this.stagedBuffer == null) {
            throw new IllegalStateException("buffer is not staged");
        }
    }

    @Override

    public void stage(int minimumLength) {
        this.throwIfAlreadyStaged();
        final var buffer = pool.rent(minimumLength);
        this.stagedBuffer = buffer;
        this.stagedLength = Array.getLength(buffer);
    }

    @Override
    public boolean tryStage(int minimumLength) {
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
    public T stagedBuffer() {
        this.throwIfNotStaged();
        return this.stagedBuffer;
    }

    @Override
    public int stagedOffset() {
        this.throwIfNotStaged();
        return 0;
    }

    @Override
    public int stagedLength() {
        this.throwIfNotStaged();
        return this.stagedLength;
    }

    @Override
    public void finish(int written) {
        this.throwIfNotStaged();
        if (this.stagedLength < StreamUtil.lenof(written)) {
            throw new IllegalArgumentException();
        }
        this.callback.finished(this.stagedBuffer, 0, written);
    }

}
