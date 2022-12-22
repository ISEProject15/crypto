package project.test.attacks;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.lib.crypto.algorithm.RSA;
import project.scaffolding.IntMath;

public class HastadBroadcastAttack {
    public static void demo() {
        final var random = new Random();
        final var exponent = BigInteger.valueOf(5);
        final var k = 16;
        final var keyPairs = new RSAKeyBundle[exponent.intValue()];
        var plainLength = Integer.MAX_VALUE;
        for (var i = 0; i < keyPairs.length; ++i) {
            RSAKeyBundle pair = null;
            while (pair == null) {
                pair = generateKeyBundle(k, exponent, random);
                for (var j = 0; j < i; ++j) {
                    final var gcd = keyPairs[j].modulo.gcd(pair.modulo);
                    if (!gcd.equals(BigInteger.ONE)) {
                        pair = null;
                        break;
                    }
                }
            }

            System.out.println("keypair[" + i + "]: " + pair);
            keyPairs[i] = pair;
            plainLength = Math.min(plainLength, RSA.bitPlainBlockLength(pair.modulo));
        }

        final var plain = new BigInteger(plainLength, random);
        final var modremPairs = new BigInteger[keyPairs.length * 2];
        System.out.println("exponent: " + exponent);
        System.out.println("plain: " + plain);
        for (var i = 0; i < keyPairs.length; ++i) {
            final var pair = keyPairs[i];
            final var code = plain.modPow(exponent, pair.modulo);
            modremPairs[2 * i + 0] = pair.modulo;
            modremPairs[2 * i + 1] = code;
            System.out.println("code[" + i + "]: " + code);
        }

        final var x = IntMath.chineseReminder(modremPairs);
        System.out.println("plain**e: " + x);

        final var xRootE = Math.pow(x.doubleValue(), 1.0 / exponent.doubleValue());
        final var decoded = Math.round(xRootE);

        System.out.println("decoded: " + decoded);

        System.out.println("decoded == plain: " + (decoded == plain.longValue()));
    }

    static RSAKeyBundle generateKeyBundle(int k, BigInteger exponent, Random random) {
        BigInteger n = null;
        BigInteger phi = null;
        while (true) {
            final var pair = generatePrimePair(k, random);
            n = pair.p.multiply(pair.q);
            phi = pair.p.subtract(BigInteger.ONE).multiply(pair.q.subtract(BigInteger.ONE));
            if (phi.gcd(exponent).equals(BigInteger.ONE)) {
                break;
            }
        }

        if (phi.compareTo(exponent) <= 0) {
            return null;
        }

        final var secret = exponent.modInverse(phi);

        return RSAKeyBundle.of(n, secret, exponent);
    }

    static IntPair generatePrimePair(int k, Random random) {
        if (k < 2) {
            return null;
        }
        final var p = BigInteger.probablePrime(k, random);
        BigInteger prime = null;
        do {
            prime = BigInteger.probablePrime(k, random);
        } while (p.equals(prime));
        final var q = prime;

        return new IntPair(p, q);
    }

    private static class IntPair {
        public IntPair(BigInteger p, BigInteger q) {
            this.p = p;
            this.q = q;
        }

        public final BigInteger p, q;
    }
}