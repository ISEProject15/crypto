package project.lib.crypto.algorithm;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.scaffolding.IntMath;

public final class RSA {
    private RSA() {

    }

    // 2 ** (2k - 2) < modulo <= 2**(2k) - 1
    // -> 2k-1 <= modulo.bitLength < 2k + 1
    // <-> modulo.bitLength = 2k - 1, 2k
    public static RSAKeyBundle generateKey(int k, Random random) {
        if (k < 2) {
            return null;
        }

        final var one = BigInteger.ONE;
        // 2 ** (k - 1) < p <= (2 ** k) - 1;
        final var p = BigInteger.probablePrime(k, random);
        // p must not equal to q
        BigInteger prime = null;
        do {
            prime = BigInteger.probablePrime(k, random);
        } while (p.equals(prime));
        final var q = prime;

        // 2 ** (2k - 2) < modulo <= ((2**k) - 1) ** 2
        // ((2**k) - 1)**2 = 2**(2k) - 2**(k + 1) + 1
        // 2**(k + 1) >= 2
        // -> -2**(k + 1) + 1 <= -1
        // -> modulo <= 2**(2k) - 1
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
        var num = one.shiftLeft(len).subtract(one);
        while (true) {
            // number that filled 1 and its bit length is len - 1
            if (num.compareTo(modulo) < 0) {
                break;
            }
            num = num.shiftRight(1);
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
