package project.test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import static project.lib.scaffolding.debug.BinaryDebug.*;

import project.lib.StreamBuffer;
import project.lib.StreamUtil;
import project.lib.Transformer;

abstract class RSAPlain {

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

    private static int ceil(int num, int div) {
        return (num + div - 1) / div;
    }

    public static RSAKeyBundle generateKey(int k) {
        final var one = BigInteger.ONE;
        final var random = new Random();
        final var p = BigInteger.probablePrime(k, random);
        final var q = BigInteger.probablePrime(k, random);

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

    public static Transformer encripter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer decripter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static class Encrypter implements Transformer {
        public Encrypter(BigInteger exponent, BigInteger modulo) {
            this.exponent = exponent;
            this.modulo = modulo;
            final var plainBlockLength = bitPlainBlockLength(modulo) / 8;
            this.codeBlockLength = ceil(modulo.bitLength(), 8);
            this.plainBlock = new byte[plainBlockLength];
            this.buffer = new StreamBuffer();
            this.blockRemaining = 0;
            this.ended = false;
        }

        private int blockRemaining;
        private final byte[] plainBlock;
        private final int codeBlockLength;
        private final BigInteger exponent, modulo;
        private final StreamBuffer buffer;
        private boolean ended;

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset,
                int destinationLength) {
            if (!this.ended) {
                this.writeToStreamingBuffer(source, sourceOffset, sourceLength);
            }
            return this.buffer.read(destination, destinationOffset, destinationLength);
        }

        private void writeToStreamingBuffer(byte[] source, int sourceOffset, int sourceLength) {
            final var plainBlock = this.plainBlock;
            final var plainBlockLength = plainBlock.length;
            final var codeBlockLength = this.codeBlockLength;
            final var isSourceLast = StreamUtil.isLast(sourceLength);
            final var length = StreamUtil.lenof(sourceLength);
            this.ended = StreamUtil.isLast(sourceLength);
            var blockRemaining = this.blockRemaining;
            final var totalLength = length + blockRemaining;
            final var outputBlockCount = isSourceLast
                    ? ceil(totalLength, plainBlockLength)
                    : totalLength / plainBlockLength;
            final var segmentLength = outputBlockCount * codeBlockLength;
            final var segment = this.buffer.stage(segmentLength);

            var written = 0;
            var read = 0;
            while (true) {
                final var len = Math.min(length - read, plainBlockLength - blockRemaining);
                System.arraycopy(source, sourceOffset + read, plainBlock, blockRemaining, len);
                blockRemaining += len;
                read += len;
                Arrays.fill(plainBlock, blockRemaining, plainBlockLength, (byte) 0);

                final var remain = blockRemaining < plainBlockLength;
                if (blockRemaining == 0 || !isSourceLast && remain) {
                    break;
                }

                System.out.println("plain:" + dumpHex(plainBlock));
                final var plain = new BigInteger(1, plainBlock);
                final var code = plain.modPow(exponent, modulo);
                // code satisfies 0 <= code < modulo, so bin length is outputBlockLength at most
                final var bin = code.toByteArray();
                System.out.println("code: " + dumpHex(bin));
                // bin.length may exceeds inputBlockLength
                final var l = Math.min(codeBlockLength, bin.length);
                final var o = bin.length - l;
                System.arraycopy(bin, o, segment.buffer,
                        written + (codeBlockLength - l) + segment.offset(), l);

                written += codeBlockLength;
                blockRemaining = 0;

                if (isSourceLast && remain) {
                    break;
                }
            }

            this.blockRemaining = blockRemaining;
            this.buffer.notifyWritten(segmentLength);
        }

    }

    private static class Decrypter implements Transformer {
        public Decrypter(BigInteger secret, BigInteger modulo) {
            this.secret = secret;
            this.modulo = modulo;
            final var codeBlockLength = ceil(modulo.bitLength(), 8);
            this.plainBlockLength = bitPlainBlockLength(modulo) / 8;
            this.block = new byte[codeBlockLength];
            this.buffer = new StreamBuffer();
            this.ended = false;
        }

        private final int plainBlockLength;
        private int blockRemaining;
        private final byte[] block;
        private final BigInteger secret;
        private final BigInteger modulo;
        private final StreamBuffer buffer;
        private boolean ended;

        @Override
        public int estimatedOutputSize(int sourceLength) {
            return -1;
        }

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset, int destinationLength) {
            if (!this.ended) {
                this.writeToStreamingBuffer(source, sourceOffset, sourceLength);
            }
            return this.buffer.read(destination, destinationOffset, destinationLength);
        }

        private void writeToStreamingBuffer(byte[] source, int offset, int sourceLength) {
            final var block = this.block;
            final var codeBlockLength = block.length;
            final var plainBlockLength = this.plainBlockLength;
            final var isSourceLast = StreamUtil.isLast(sourceLength);
            final var length = StreamUtil.lenof(sourceLength);
            var blockRemaining = this.blockRemaining;
            final var total = length + blockRemaining;
            final var outputLength = isSourceLast
                    ? ceil(total, codeBlockLength)
                    : total / codeBlockLength;
            final var segmentLength = outputLength * plainBlockLength;
            final var segment = this.buffer.stage(segmentLength);
            this.ended = StreamUtil.isLast(sourceLength);
            var written = 0;
            var read = 0;
            while (true) {
                final var len = Math.min(length - read, codeBlockLength - blockRemaining);
                System.arraycopy(source, offset + read, block, blockRemaining, len);
                blockRemaining += len;
                read += len;
                Arrays.fill(block, blockRemaining, codeBlockLength, (byte) 0);

                final var remain = blockRemaining < codeBlockLength;
                if (blockRemaining == 0 || !isSourceLast && remain) {
                    break;
                }

                System.out.println("code: " + dumpHex(block));
                final var code = new BigInteger(1, block);
                final var plain = code.modPow(secret, modulo);
                final var bin = plain.toByteArray();
                System.out.println("plain:" + dumpHex(bin));
                // bin.length may differ from inputBlockLength

                final var l = Math.min(plainBlockLength, bin.length);
                final var o = bin.length - l;
                System.arraycopy(bin, o, segment.buffer, written + (plainBlockLength - l) + segment.offset(), l);
                written += plainBlockLength;
                blockRemaining = 0;

                if (isSourceLast && remain) {
                    break;
                }
            }

            this.buffer.notifyWritten(segmentLength);
            this.blockRemaining = blockRemaining;
        }

    }
}