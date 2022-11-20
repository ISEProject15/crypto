package project.lib.crypto;

import project.lib.protocol.Ion;

public interface CipherInitInfo {
    public Ion encryption();

    public Ion decryption();
}
