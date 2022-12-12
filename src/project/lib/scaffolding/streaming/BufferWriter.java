package project.lib.scaffolding.streaming;

import project.lib.scaffolding.collections.Sequence;

public interface BufferWriter<T> {
    public void stage(int minimumLength);

    public boolean tryStage(int minimumLength);

    public T stagedBuffer();

    public int stagedOffset();

    public int stagedLength();

    public void finish(int written);

    public default void write(T source, int offset, int length) {
        this.stage(StreamUtil.lenof(length));
        final var buf = this.stagedBuffer();
        final var off = this.stagedOffset();
        System.arraycopy(source, offset, buf, off, StreamUtil.lenof(length));
        this.finish(length);
    }

    public default void write(Sequence<T> sequence, boolean completed) {
        final var iter = sequence.iterator();
        while (iter.hasNext()) {
            iter.move();
            final var srcBuffer = iter.currentBuffer();
            final var srcOffset = iter.currentOffset();
            final var srcLength = iter.currentLength();

            this.stage(srcLength);
            final var dstBuffer = this.stagedBuffer();
            final var dstOffset = this.stagedOffset();

            System.arraycopy(srcBuffer, srcOffset, dstBuffer, dstOffset, srcLength);

            if (completed && !iter.hasNext()) {
                this.finish(~srcLength);
            } else {
                this.finish(srcLength);
            }
        }
    }

    public default BufferWriter<T> transform(Transformer<T> transformer) {
        return new TransformedBufferWriter<>(this, transformer);
    }
}
