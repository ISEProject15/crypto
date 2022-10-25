public interface Encrypter {
    // encrypt source, write destination, and return wrote bytes count
    public int encrypt(byte[] source, byte[] destination);
}