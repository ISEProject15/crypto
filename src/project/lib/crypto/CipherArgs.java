package project.lib.crypto;

import project.lib.protocol.Ion;

public interface CipherArgs {
    public Ion encryptionArgs();

    public Ion decryptionArgs();
}
