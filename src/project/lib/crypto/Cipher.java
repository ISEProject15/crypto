package project.lib.crypto;

import project.lib.Transformer;

public interface Cipher {
    public Transformer encrypter(Object encryptionKey);

    public Transformer decrypter(Object decryptionKey);
}
