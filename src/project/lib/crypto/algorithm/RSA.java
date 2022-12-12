package project.lib.crypto.algorithm;

import java.math.BigInteger;
import java.util.Random;

import project.lib.scaffolding.IntMath;
import project.test.RSAKeyBundle;

public final class RSA {
    private RSA() {

    }

    public static RSAKeyBundle generateKey(int k) {
        if (k <= 8) {
            return null;
        }
        final var one = BigInteger.ONE;
        final var random = new Random();
        final var p = BigInteger.probablePrime(k, random);
        // p must not equal to q
        BigInteger prime = null;
        do {
            prime = BigInteger.probablePrime(k, random);
        } while (p.equals(prime));
        final var q = prime;

        final var modulo = p.multiply(q);
        final var phi = p.subtract(one).multiply(q.subtract(one));
        final var bitlen = phi.bitLength();
        BigInteger exponent = null;
        while (true) {
            final var rnd = new BigInteger(bitlen, random);
            exponent = rnd.mod(phi);
            if (exponent.gcd(phi).equals(BigInteger.ONE)) {
                break;
            }
        }
        final var secret = exponent.modInverse(phi);

        return RSAKeyBundle.of(modulo, secret, exponent);
    }

    private static int bitPlainBlockLength(BigInteger modulo) {
        // (1 << (modulo + 1)) - 1 does not satisfy restriction
        // block length less than modulo
        final var one = BigInteger.ONE;
        var len = modulo.bitLength() - 1;
        while (true) {
            // number that filled 1 and its bit length is len - 1
            final var num = one.shiftLeft(len).subtract(one);
            if (num.compareTo(modulo) < 0) {
                break;
            }
            len--;
        }
        return len;
    }

    // byte plain block length
    public static int plainBlockLength(BigInteger modulo) {
        return bitPlainBlockLength(modulo) / 8;
    }

    // byte code block length
    public static int codeBlockLength(BigInteger modulo) {
        return IntMath.ceil(modulo.bitLength(), 8);
    }

}
