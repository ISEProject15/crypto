package project.lib.scaffolding.streaming;

class TransformedBufferWriter<T> implements BufferWriter<T> {
    TransformedBufferWriter(BufferWriter<T> writer, Transformer<T> transformer) {
        this.writer = writer;
        this.transformer = transformer;
    }

    private final BufferWriter<T> writer;
    private final Transformer<T> transformer;

    @Override
    public void stage(int minimumLength) {
        this.transformer.writer().stage(minimumLength);
    }

    @Override
    public boolean tryStage(int minimumLength) {
        return this.transformer.writer().tryStage(minimumLength);
    }

    @Override
    public T stagedBuffer() {
        return this.transformer.writer().stagedBuffer();
    }

    @Override
    public int stagedOffset() {
        return this.transformer.writer().stagedOffset();
    }

    @Override
    public int stagedLength() {
        return this.transformer.writer().stagedLength();
    }

    @Override
    public void finish(int written) {
        this.transformer.writer().finish(written);
        this.writer.write(this.transformer.read(), this.transformer.completed());
    }

}
