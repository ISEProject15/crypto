public class TransformedOutletStream implements OutletStream {
    public TransformedOutletStream(OutletStream stream, Transformer transformer) {
        this.stream = stream;
        this.transformer = transformer;
    }

    private final OutletStream stream;
    private final Transformer transformer;
    private byte[] buffer;
    
    @Override
    public void write(byte[] source, int length) throws IOException {
        if(this.buffer == null) {
            this.buffer = new byte[source.length];
        }
        var written = transformer.transform(source, length, this.buffer);
        stream.write(this.buffer, written);
        if(length < 0) {//last segment written
            while(written > 0) {// data remaining in transformer
               written = transformer.transform(source, -1, this.buffer);
               stream.write(this.buffer, written);
            }
        }
    }
}