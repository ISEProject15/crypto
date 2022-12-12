package project.lib.scaffolding.streaming;

import java.io.OutputStream;

import project.lib.scaffolding.ByteArrayPool;

public class OutputStreamWriter implements BufferWriter<byte[]> {
    public OutputStreamWriter(OutputStream stream) {
        this.stream = stream;
    }

    private final OutputStream stream;
    private byte[] stagedBuffer;

    private void throwIfAlreadyStaged() {
        if (this.stagedBuffer == null) {
            return;
        }
        throw new IllegalStateException("buffer was already staged");
    }

    private void throwIfNotStaged() {
        if (this.stagedBuffer != null) {
            return;
        }
        throw new IllegalStateException("buffer is not staged");
    }

    @Override
    public void stage(int minimumLength) {
        this.throwIfAlreadyStaged();
        this.stagedBuffer = ByteArrayPool.rent(minimumLength);
    }

    @Override
    public boolean tryStage(int minimumLength) {
        this.throwIfAlreadyStaged();
        final var buffer = ByteArrayPool.tryRent(minimumLength);
        this.stagedBuffer = buffer;
        return buffer != null;
    }

    @Override
    public byte[] stagedBuffer() {
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
        return this.stagedBuffer.length;
    }

    @Override
    public void finish(int written) {
        final var buffer = this.stagedBuffer;
        this.stagedBuffer = null;
        try {
            stream.write(buffer, 0, StreamUtil.lenof(written));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ByteArrayPool.back(buffer);
    }
}