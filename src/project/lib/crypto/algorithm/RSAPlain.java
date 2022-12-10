package project.lib.crypto.algorithm;

import java.math.BigInteger;
import java.util.Random;

import project.lib.StreamBuffer;
import project.lib.StreamUtil;
import project.lib.Transformer;
import project.lib.scaffolding.IntMath;
import project.test.RSAKeyBundle;

// rsa crypto algorithm encrypt byte by byte
public class RSAPlain {
    public static int codeBlockLength(BigInteger modulo) {
        return IntMath.ceil(modulo.bitLength(), 8);
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

    public static Transformer encrypter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer decrypter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static class Encrypter implements Transformer {
        public Encrypter(BigInteger exponent, BigInteger modulo) {
            this.exponent = exponent;
            this.modulo = modulo;
            this.buffer = new StreamBuffer();
            this.codeBlockLength = codeBlockLength(modulo);
            this.ended = false;
        }

        private final BigInteger exponent, modulo;
        private final StreamBuffer buffer;
        private final int codeBlockLength;
        private boolean ended;

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset, int destinationLength) {

            final var srcLen = StreamUtil.lenof(sourceLength);

            for (var i = 0; i < srcLen; ++i) {

            }
            return 0;
        }

    }

    private static class Decrypter implements Transformer {
        public Decrypter(BigInteger secret, BigInteger modulo) {

        }

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset, int destinationLength) {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
