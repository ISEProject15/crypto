package project.crypto;

public interface Cipher {
    public Transformer encrypter(Object encryptionKey);

    public Transformer decrypter(Object decryptionKey);
}
