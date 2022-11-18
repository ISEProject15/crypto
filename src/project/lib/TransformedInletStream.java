public class TransformedInletStream implements InletStream {
    public TransformedInletStream(InletStream source, Transformer transformer) {
        this.source = source;
        this.transformer = transformer;
    }

    private final InletStream source;
    private final Transformer transformer;
    private byte[] buffer;
    
    @Override
    public int read(byte[] destination) throws IOException {
        if(this.buffer == null) {
            this.buffer = new byte[destination.length];
        }
        final var bufWritten = this.source.read(this.buffer);
        return transformer.transform(this.buffer, bufWritten, destination);
    }
}