public interface Decrypter 
{
    // decrypt source, write destination, and return wrote bytes count
    public int decrypt(byte[] source, byte[] destination);
}