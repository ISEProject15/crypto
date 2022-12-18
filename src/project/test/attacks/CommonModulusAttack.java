package project.test.attacks;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.lib.crypto.algorithm.RSA;
import project.scaffolding.IntMath;

public class CommonModulusAttack {
    public static void demo() {
        final var random = new Random();
        final var moduloPhi = generateModuloPhi(4, random);

        RSAKeyBundle key0, key1;
        do {
            key0 = generateKey(moduloPhi, random);
            key1 = generateKey(moduloPhi, random);
        } while (key0.exponent.equals(key1.exponent));
        {
            final var duv = IntMath.extendedEuclidean(key0.exponent, key1.exponent);
            final var u = duv[1];
            final var v = duv[2];

        }

        final var plainBlockLength = RSA.plainBlockLength(moduloPhi.modulo);
        final var plain = new BigInteger(plainBlockLength * 8, random);

        final var code0 = plain.modPow(key0.exponent, moduloPhi.modulo);
        final var code1 = plain.modPow(key1.exponent, moduloPhi.modulo);
        final var gcd0 = code0.gcd(moduloPhi.modulo);
        if (!gcd0.equals(BigInteger.ONE)) {// when code is weak code
            final var p = gcd0;
            final var q = moduloPhi.modulo.divide(p);
            final var phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            final var secret = key0.exponent.modInverse(phi);

            final var decoded = code0.modPow(secret, moduloPhi.modulo);
            System.out.println(decoded);
            System.out.println(decoded.equals(plain));
            return;
        }
        final var gcd1 = code1.gcd(moduloPhi.modulo);
        if (!gcd1.equals(BigInteger.ONE)) {// when code is weak code
            final var p = gcd1;
            final var q = moduloPhi.modulo.divide(p);
            final var phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            final var secret = key1.exponent.modInverse(phi);

            final var decoded = code1.modPow(secret, moduloPhi.modulo);
            System.out.println(decoded);
            System.out.println(decoded.equals(plain));
            return;
        }

        IntMath.extendedEuclidean(code1, moduloPhi.modulo);
    }

    public static ModuloPhi generateModuloPhi(int k, Random random) {
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

        return new ModuloPhi(p.multiply(q), p.subtract(one).multiply(q.subtract(one)));
    }

    public static RSAKeyBundle generateKey(ModuloPhi pair, Random random) {
        final var phi = pair.phi;
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
        return RSAKeyBundle.of(pair.modulo, secret, exponent);
    }

    private static class ModuloPhi {
        public ModuloPhi(BigInteger modulo, BigInteger phi) {
            this.modulo = modulo;
            this.phi = phi;
        }

        public final BigInteger modulo;
        public final BigInteger phi;
    }
}
