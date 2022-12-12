package project.lib.scaffolding.streaming;

import java.io.IOException;
import java.io.InputStream;

import project.lib.StreamBuffer;
import project.lib.scaffolding.collections.Sequence;

public class InputStreamReader implements StreamReader<byte[]> {
    public InputStreamReader(InputStream stream) {
        this.stream = stream;
        this.buffer = new StreamBuffer();
        this.completed = false;
    }

    private final InputStream stream;
    private final StreamBuffer buffer;
    private boolean completed;

    @Override
    public void advance(int consumed) {
        this.buffer.discard(consumed);
    }

    @Override
    public Sequence<byte[]> read(int least) throws IOException {
        if (least <= 0) {
            return this.buffer;
        }
        if (this.completed) {
            return this.buffer;
        }

        final var writer = this.buffer.writer();
        writer.stage(least + 1);
        final var buf = writer.stagedBuffer();
        final var off = writer.stagedOffset();
        final var len = writer.stagedLength();
        final var written = this.stream.read(buf, off, len - 1);
        if (written < 0) {
            this.completed = true;
            writer.finish(0);
            return this.buffer;
        }
        final var next = this.stream.read();
        if (next < 0) {
            this.completed = true;
            writer.finish(written);
        } else {
            buf[written] = (byte) next;
            writer.finish(written + 1);
        }
        return this.buffer;
    }

    @Override
    public boolean completed() {
        return this.completed;
    }

}
