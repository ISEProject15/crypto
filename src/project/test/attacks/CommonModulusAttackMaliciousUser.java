package project.test.attacks;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.scaffolding.IntMath;

public class CommonModulusAttackMaliciousUser {
    public static void demo() {
        final var random = new Random();
        final var primePair = generatePrimePair(6, random);
        System.out.println("p: " + primePair.p);
        System.out.println("q: " + primePair.q);
        final var modulo = primePair.p.multiply(primePair.q);
        final var phi = primePair.p.subtract(BigInteger.ONE).multiply(primePair.q.subtract(BigInteger.ONE));
        System.out.println("modulo: " + modulo);
        System.out.println("   phi: " + phi);
        final var keyNormal = generateKey(modulo, phi, random);
        final var keyMalicious = generateKey(modulo, phi, random);

        System.out.println("   normal user exponent: " + keyNormal.exponent);
        System.out.println("malicious user exponent: " + keyMalicious.exponent);

        final var mod = keyMalicious.exponent.multiply(keyMalicious.secret).subtract(BigInteger.ONE);
        final var duv = IntMath.extendedEuclidean(keyNormal.exponent, mod);
        var altKey = duv[1];
        while (altKey.compareTo(BigInteger.ZERO) < 0) {
            altKey = altKey.add(mod);
        }

        System.out.println("normal user key alt: " + altKey);

        final var plain = new BigInteger(modulo.bitLength() - 1, random);
        System.out.println("plain: " + plain);
        if (plain.equals(BigInteger.ZERO)) {
            System.out.println("plain is weak");
            return;
        }

        final var code = plain.modPow(keyNormal.exponent, modulo);
        final var decoded = code.modPow(altKey, modulo);

        System.out.println("decoded: " + decoded);
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

    public static RSAKeyBundle generateKey(BigInteger modulo, BigInteger phi, Random random) {
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

    private static class IntPair {
        public IntPair(BigInteger p, BigInteger q) {
            this.p = p;
            this.q = q;
        }

        public final BigInteger p, q;
    }
}
