package project.lib.scaffolding.streaming;

import java.lang.reflect.Array;

import project.lib.scaffolding.collections.ArrayUtil;
import project.scaffolding.debug.BinaryDebug;

public final class BlockBufferWriterProxy<T> implements BufferWriterListener<T> {
    public static <T> BlockBufferWriterProxy<T> wrap(T block, BufferWriterListener<T> listener) {
        return new BlockBufferWriterProxy<>(block, listener);
    }

    private BlockBufferWriterProxy(T block, BufferWriterListener<T> listener) {
        this.listener = listener;
        this.block = block;
        this.blockLength = Array.getLength(block);
        this.blockRemaining = 0;
    }

    // this listener always called
    // with argument length is blockLength or ~blockLength
    private final BufferWriterListener<T> listener;
    private final T block;
    private final int blockLength;
    private int blockRemaining;

    public T getBlock() {
        return this.block;
    }

    public int blockLength() {
        return this.blockLength;
    }

    @Override
    public void finished(T buffer, int offset, int length) {
        final var listener = this.listener;
        final var block = this.block;
        final var blockLength = this.blockLength;
        final var lastInput = StreamUtil.isLast(length);
        final var bufferLast = offset + StreamUtil.lenof(length);
        var bufferOffset = offset;
        // if data remain in the block,
        // copy buffer to block and fill the block then invoke the listener
        if (this.blockRemaining > 0) {
            final var blockRest = blockLength - this.blockRemaining;
            final var copyLen = Math.min(blockRest, bufferLast - bufferOffset);
            System.arraycopy(buffer, bufferOffset, block, this.blockRemaining, copyLen);
            this.blockRemaining += copyLen;
            bufferOffset += copyLen;

            // if block is filled, invoke the listener then clear block
            if (this.blockRemaining >= blockLength) {
                listener.finished(block, 0, blockLength);
                this.blockRemaining = 0;
            }
            // NOTE: if block is not filled, buffer is now empty.
        }

        while (true) {
            if ((bufferLast - bufferOffset) <= blockLength) {
                break;
            }
            assert (bufferLast - bufferOffset) > blockLength;
            listener.finished(buffer, bufferOffset, blockLength);
            bufferOffset += blockLength;
        }

        final var bufferRest = bufferLast - bufferOffset;
        assert bufferRest <= blockLength;
        if (bufferRest == blockLength) {
            listener.finished(buffer, bufferOffset, lastInput ? ~blockLength : blockLength);
            return;
        } else {
            assert bufferRest < blockLength;
            // if data remain in block, buffer is already empty and nothing will change here
            System.arraycopy(buffer, bufferOffset, block, this.blockRemaining, bufferRest);
            this.blockRemaining += bufferRest;
            bufferOffset += bufferRest;
        }

        // if input is last and data remain in the block,
        // padding the block and invoke the listener with last marker
        if (lastInput && this.blockRemaining > 0) {
            ArrayUtil.clear(block, this.blockRemaining, blockLength);
            listener.finished(block, 0, ~blockLength);
            this.blockRemaining = 0;
        }
    }

}
