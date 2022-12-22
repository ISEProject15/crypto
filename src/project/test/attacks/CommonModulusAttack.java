package project.test.attacks;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.lib.crypto.algorithm.RSA;
import project.scaffolding.IntMath;

public class CommonModulusAttack {
    public static void demo() {
        final var random = new Random();
        final var primePair = generatePrimePair(6, random);
        System.out.println("p: " + primePair.p);
        System.out.println("q: " + primePair.q);
        final var modulo = primePair.p.multiply(primePair.q);
        final var phi = primePair.p.subtract(BigInteger.ONE).multiply(primePair.q.subtract(BigInteger.ONE));
        System.out.println("modulo: " + modulo);
        System.out.println("phi: " + phi);
        RSAKeyBundle key0, key1;
        do {
            key0 = generateKey(modulo, phi, random);
            key1 = generateKey(modulo, phi, random);
        } while (key0.exponent.equals(key1.exponent));

        System.out.println("key0 exponent: " + key0.exponent);
        System.out.println("key1 exponent: " + key1.exponent);

        BigInteger u, v, gcd;
        {
            final var duv = IntMath.extendedEuclidean(key0.exponent, key1.exponent);
            gcd = duv[0];
            u = duv[1];
            v = duv[2];
            if (u.compareTo(v) < 0) {// ensure u > 0 > v
                final var tmp0 = v;
                v = u;
                u = tmp0;
                final var tmp1 = key1;
                key1 = key0;
                key0 = tmp1;
            }
        }

        System.out.println("gcd(key0.exp, key1.exp): " + gcd);

        if (!gcd.equals(BigInteger.ONE)) {
            System.out.println("failed; gcd is not one.");
            return;
        }

        final var plainBlockLength = RSA.plainBlockLength(modulo);
        final var plain = new BigInteger(plainBlockLength * 8, random);
        System.out.println("plain: " + plain);
        final var code0 = plain.modPow(key0.exponent, modulo);
        final var code1 = plain.modPow(key1.exponent, modulo);
        System.out.println("code0: " + code0);
        System.out.println("code1: " + code1);

        if (code1.equals(BigInteger.ZERO)) {
            System.out.println("plain is weak");
            return;
        }

        final var duv = IntMath.extendedEuclidean(code1, modulo);
        final var gcd1 = duv[0];
        if (!gcd1.equals(BigInteger.ONE)) {// when code1 is weak code
            System.out.println("code1 is weak");
            System.out.println(code0.gcd(modulo));
            final var p = gcd1;
            final var q = modulo.divide(p);
            System.out.println("estimated p: " + p);
            System.out.println("estimated q: " + q);
            final var estimatedPhi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            final var secret = key1.exponent.modInverse(estimatedPhi);

            final var decoded = code1.modPow(secret, modulo);
            System.out.println("decoded: " + decoded);
            System.out.println("decoded == plain: " + decoded.equals(plain));
            return;// success
        }
        final var invCode1 = duv[1];

        final var code0powU = code0.modPow(u, modulo);
        final var code1powV = invCode1.modPow(v.negate(), modulo);

        final var decoded = code0powU.multiply(code1powV).mod(modulo);

        System.out.println("decoded: " + decoded);

        System.out.println("plain = decoded: " + plain.equals(decoded));
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
