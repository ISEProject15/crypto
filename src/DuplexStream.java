public interface DuplexStream extends java.io.Closeable {

    public int read(byte[] destination);

    public void write(byte[] source);
}