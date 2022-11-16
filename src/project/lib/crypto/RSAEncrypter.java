package project.lib.crypto;

import java.math.BigInteger;

public class RSAEncrypter implements project.lib.Transformer {
    private final BigInteger modulo;
    private final BigInteger exponent;

    private RSAEncrypter(BigInteger modulo, BigInteger exponent) {
        this.modulo = modulo;
        this.exponent = exponent;
    }

    @Override
    public int transform(int sourceLength, byte[] source, byte[] destination) {

        return 0;
    }
    // TODO: 入力ブロック長と出力ブロック長を計算する関数を考える．

}