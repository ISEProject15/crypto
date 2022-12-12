package project.lib.scaffolding.streaming;

import project.lib.scaffolding.ByteArrayPool;

public class TransformedBufferWriter implements BufferWriter<byte[]> {
    TransformedBufferWriter(BufferWriter<byte[]> writer, Transformer transformer) {
        this.writer = writer;
        this.transformer = transformer;
        this.bufferWriter = new DefaultPooledBufferWriter<byte[]>(ByteArrayPool.instance(), this::onFinished);
    }

    private final BufferWriter<byte[]> writer;
    private final Transformer transformer;
    private final DefaultPooledBufferWriter<byte[]> bufferWriter;

    private void onFinished(byte[] buffer, int offset, int length) {
        
    }

    @Override
    public void stage(int minimumLength) {
        this.bufferWriter.stage(minimumLength);
    }

    @Override
    public boolean tryStage(int minimumLength) {
        return this.bufferWriter.tryStage(minimumLength);
    }

    @Override
    public byte[] stagedBuffer() {
        return this.bufferWriter.stagedBuffer();
    }

    @Override
    public int stagedOffset() {
        return this.bufferWriter.stagedOffset();
    }

    @Override
    public int stagedLength() {
        return this.bufferWriter.stagedLength();
    }

    @Override
    public void finish(int written) {
        this.bufferWriter.finish(written);
    }
}
