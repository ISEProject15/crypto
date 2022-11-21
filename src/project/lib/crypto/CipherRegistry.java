package project.lib.crypto;

import project.lib.protocol.Ion;

public interface CipherRegistry {
    public Cipher resolve(Ion object);
}
