package project.test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import project.lib.StreamUtil;

abstract class RSAPlain {

    private static int bitInputBlockLength(BigInteger modulo) {
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

    private static int ceil(int num, int div) {
        return (num + div - 1) / div;
    }

    private static void assertPrime(BigInteger num) {
        if (!num.testBit(0))// num is even
            throw new IllegalStateException("num is even");
        final var bound = num.sqrt();
        var div = BigInteger.valueOf(3);
        do {
            if (num.remainder(div).equals(BigInteger.ZERO)) {
                throw new IllegalStateException("num is a multiple of " + div.toString());
            }
            div = div.add(BigInteger.TWO);
        } while (div.compareTo(bound) < 0);
    }

    public static RSAKeyBundle generateKey(int k) {
        final var random = new Random();
        final var p = BigInteger.probablePrime(k, random);
        final var q = BigInteger.probablePrime(k, random);
        assertPrime(p);
        assertPrime(q);

        final var modulo = p.multiply(q);
        final var phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
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

    public static Transformer encripter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer decripter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static class Encrypter extends Transformer {
        public Encrypter(BigInteger exponent, BigInteger modulo) {
            this.exponent = exponent;
            this.modulo = modulo;
            final var inputBlockLength = bitInputBlockLength(modulo) / 8;
            this.outputBlockLength = ceil(modulo.bitLength(), 8);
            this.buffer = new byte[inputBlockLength];
            this.remaining = 0;
        }

        private int remaining;
        private final byte[] buffer;
        private final int outputBlockLength;
        private final BigInteger exponent, modulo;

        @Override
        public byte[] transform(byte[] source, int offset, int sourceLength) {
            final var buffer = this.buffer;
            final var inputBlockLength = buffer.length;
            final var outputBlockLength = this.outputBlockLength;
            final var isLast = StreamUtil.isLast(sourceLength);
            final var length = StreamUtil.lenof(sourceLength);
            var remaining = this.remaining;
            final var total = length + remaining;
            final var outputLength = isLast
                    ? ceil(total, inputBlockLength)
                    : total / inputBlockLength;
            final byte[] output = new byte[outputLength * outputBlockLength];

            var written = 0;
            var read = 0;
            while (read < length) {
                final var len = Math.min(length - read, inputBlockLength - remaining);
                System.arraycopy(source, offset + read, buffer, remaining, len);
                remaining += len;
                read += len;
                Arrays.fill(buffer, remaining, inputBlockLength, (byte) 0);

                final var remain = remaining < inputBlockLength;
                if (!isLast && remain) {
                    break;
                }

                final var plain = new BigInteger(1, buffer);
                final var code = plain.modPow(exponent, modulo);
                // code satisfies 0 <= code < modulo, so bin length is outputBlockLength at most
                final var bin = code.toByteArray();
                // bin.length may exceeds inputBlockLength
                final var l = Math.min(outputBlockLength, bin.length);
                System.arraycopy(bin, 0, output, written + (outputBlockLength - l), l);

                written += outputBlockLength;
                remaining = 0;

                if (isLast && remain) {
                    break;
                }
            }

            this.remaining = remaining;
            return output;
        }

    }

    private static class Decrypter extends Transformer {
        public Decrypter(BigInteger secret, BigInteger modulo) {
            this.secret = secret;
            this.modulo = modulo;
            final var outputBlockLength = ceil(modulo.bitLength(), 8);
            this.inputBlockLength = bitInputBlockLength(modulo) / 8;
            this.buffer = new byte[outputBlockLength];
        }

        private final int inputBlockLength;
        private int remaining;
        private final byte[] buffer;
        private final BigInteger secret;
        private final BigInteger modulo;

        @Override
        public byte[] transform(byte[] source, int offset, int sourceLength) {
            final var buffer = this.buffer;
            final var outputBlockLength = buffer.length;
            final var inputBlockLength = this.inputBlockLength;
            final var isLast = StreamUtil.isLast(sourceLength);
            final var length = StreamUtil.lenof(sourceLength);
            var remaining = this.remaining;
            final var total = length + remaining;
            final var outputLength = isLast
                    ? ceil(total, outputBlockLength)
                    : total / outputBlockLength;
            final byte[] output = new byte[outputLength * inputBlockLength];

            var written = 0;
            var read = 0;
            while (read < length) {
                final var len = Math.min(length - read, outputBlockLength - remaining);
                System.arraycopy(source, offset + read, buffer, remaining, len);
                remaining += len;
                read += len;
                Arrays.fill(buffer, remaining, outputBlockLength, (byte) 0);

                final var remain = remaining < outputBlockLength;
                if (!isLast && remain) {
                    break;
                }

                final var code = new BigInteger(1, buffer);
                final var plain = code.modPow(secret, modulo);
                final var bin = plain.toByteArray();
                // bin.length may exceeds inputBlockLength
                final var l = Math.min(inputBlockLength, bin.length);
                System.arraycopy(bin, 0, output, written + (inputBlockLength - l), l);
                written += inputBlockLength;
                remaining = 0;

                if (isLast && remain) {
                    break;
                }
            }

            this.remaining = remaining;
            return output;
        }

    }
}