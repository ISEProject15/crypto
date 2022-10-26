public interface Cipher {
    public String identity();

    public Transformer encrypter();

    public Transformer decrypter();
}
