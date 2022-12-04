package project.lib.crypto;

import project.lib.Transformer;
import project.lib.protocol.Ion;

public interface Cipher {
    public String identity();

    // 引数を元にencrypterとdecrypterの初期化に必要なデータを生成する
    // argsがnullの時はデフォルトの引数で初期化する
    public CipherArgs create(Ion args);

    public Transformer encrypter(Ion encryptionKey);

    public Transformer decrypter(Ion decryptionKey);
}
