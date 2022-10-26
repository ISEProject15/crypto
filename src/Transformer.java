public interface Transformer {
    // transform source, write destination, and return wrote bytes count
    // additional flags may needed
    public int transform(byte[] source, byte[] destination);
}
