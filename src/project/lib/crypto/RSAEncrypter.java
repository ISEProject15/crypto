package project.lib.crypto;

import java.math.BigInteger;

public class RSAEncrypter implements project.lib.Transformer {
    private final BigInteger module;
    private final BigInteger exponent;

    @Override
    public int transform(int sourceLength, byte[] source, byte[] destination) {

        return 0;
    }
    // TODO: 入力ブロック長と出力ブロック長を計算する関数を考える．

}
