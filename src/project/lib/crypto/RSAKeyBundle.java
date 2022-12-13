package project.lib.crypto;

import java.math.BigInteger;

public class RSAKeyBundle {
    public static RSAKeyBundle of(BigInteger modulo, BigInteger secret, BigInteger exponent) {
        return new RSAKeyBundle(modulo, secret, exponent);
    }

    private RSAKeyBundle(BigInteger modulo, BigInteger secret, BigInteger exponent) {
        this.modulo = modulo;
        this.secret = secret;
        this.exponent = exponent;
    }

    public final BigInteger modulo;
    public final BigInteger secret;
    public final BigInteger exponent;

    @Override
    public String toString() {
        return "{exponent:" + exponent + ",secret:" + secret + ",modulo:" + modulo + "}";
    }
}
