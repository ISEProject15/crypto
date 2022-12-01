package project.lib;

import java.io.IOException;

public class TransformedInletStream implements InletStream {
    public TransformedInletStream(InletStream source, Transformer transformer) {
        this.source = source;
        this.transformer = transformer;
        final var preferred = source.preferredBufferSize();
        if (preferred > 0) {
            this.buffer = new byte[preferred];
        }
    }

    private final InletStream source;
    private final Transformer transformer;
    private byte[] buffer;

    @Override
    public int read(byte[] destination, int offset, int length) throws IOException {
        if (destination == null || offset < 0 || length < 0 || offset + length > destination.length) {
            throw new IllegalArgumentException();
        }
        if (this.buffer == null) {
            this.buffer = new byte[destination.length];
        }

        final var bufWritten = this.source.read(this.buffer);
        return transformer.transform(this.buffer, bufWritten, destination, offset, length);
    }

    @Override
    public int preferredBufferSize() {
        if (this.buffer == null) {
            return -1;
        }
        return transformer.estimatedOutputSize(this.buffer.length);
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }
}