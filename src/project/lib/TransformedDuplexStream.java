package project.lib;

import java.io.IOException;

public class TransformedDuplexStream implements DuplexStream {
    private static int defaultBufferSize = 1024;

    public TransformedDuplexStream(DuplexStream stream, Transformer incoming, Transformer outgoing) {
        this.incomingTransformer = incoming;
        this.outgoingTransformer = outgoing;
        this.baseStream = stream;
        this.incomingBuffer = new byte[defaultBufferSize];
        this.outgoingBuffer = new byte[defaultBufferSize];
        this.incomingLock = new Object();
        this.outgoingLock = new Object();
    }

    public final Transformer incomingTransformer;
    public final Transformer outgoingTransformer;
    public final DuplexStream baseStream;

    private byte[] incomingBuffer;
    private byte[] outgoingBuffer;
    private final Object incomingLock;
    private final Object outgoingLock;

    @Override
    public synchronized void close() throws IOException {
        baseStream.close();
    }

    @Override
    public int read(byte[] destination) {
        synchronized (this.incomingLock) {
            final var buffer = this.incomingBuffer;
            final var red = this.baseStream.read(buffer);
            return this.incomingTransformer.transform(red, buffer, destination);
        }
    }

    @Override
    public void write(byte[] source, int length) {
        synchronized (this.outgoingLock) {
            final var buffer = this.outgoingBuffer;
            final var len = this.outgoingTransformer.transform(length, source, buffer);
            this.baseStream.write(buffer, len);
        }
    }

}
