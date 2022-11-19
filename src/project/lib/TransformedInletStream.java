package project.lib;

import java.io.IOException;

public class TransformedInletStream implements InletStream {
    public TransformedInletStream(InletStream source, Transformer transformer) {
        this.source = source;
        this.transformer = transformer;
        final var preferred = source.preferredBufferSize();
        if (preferred > 0) {
            final var estimated = transformer.estimatedOutputSize(preferred);
            if (estimated > 0) {
                this.buffer = new byte[estimated];
            }
        }
    }

    private final InletStream source;
    private final Transformer transformer;
    private byte[] buffer;

    @Override
    public int read(byte[] destination) throws IOException {
        if (this.buffer == null) {
            this.buffer = new byte[destination.length];
        }
        final var bufWritten = this.source.read(this.buffer);
        return transformer.transform(this.buffer, bufWritten, destination);
    }

    @Override
    public int preferredBufferSize() {
        if (this.buffer == null) {
            return -1;
        }
        return this.buffer.length;
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }
}