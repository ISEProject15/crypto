package project.lib;

import java.io.IOException;

public class TransformedOutletStream implements OutletStream {
    public TransformedOutletStream(OutletStream stream, Transformer transformer) {
        this.stream = stream;
        this.transformer = transformer;
    }

    private final OutletStream stream;
    private final Transformer transformer;
    private byte[] buffer;

    @Override
    public void write(byte[] source, int offset, int length) throws IOException {
        final var estimated = transformer.estimatedOutputSize(length);
        if (this.buffer == null || estimated > this.buffer.length) {
            this.buffer = new byte[Math.max(estimated, source.length)];
        }
        var written = transformer.transform(source, 0, length, this.buffer, 0, this.buffer.length);
        stream.write(this.buffer, 0, written);
        if (length < 0) {// last segment written
            while (written > 0) {// data remaining in transformer
                written = transformer.transform(source, 0, -1, this.buffer, 0, this.buffer.length);
                stream.write(this.buffer, 0, written);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }
}