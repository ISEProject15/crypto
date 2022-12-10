package project.lib.crypto.algorithm;

import static project.scaffolding.debug.BinaryDebug.*;

import java.math.BigInteger;
import java.util.Arrays;

import project.lib.StreamBuffer;
import project.lib.StreamUtil;
import project.lib.Transformer;
import project.lib.scaffolding.IntMath;
import project.test.RSAKeyBundle;

// RSA crypto algorithm encrypt chunk by chunk 
public class RSAPlainChunked {
    public static RSAKeyBundle generateKey(int k) {
        return RSAPlain.generateKey(k);
    }

    public static Transformer encripter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer decripter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
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

    private static class Encrypter implements Transformer {
        public Encrypter(BigInteger exponent, BigInteger modulo) {
            this.exponent = exponent;
            this.modulo = modulo;
            final var plainBlockLength = plainBlockLength(modulo);
            this.codeBlockLength = codeBlockLength(modulo);
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
        public int maximumOutputSize(int sourceLength) {
            final var len = StreamUtil.lenof(sourceLength);
            final var total = this.blockRemaining + len;
            if (StreamUtil.isLast(sourceLength)) {
                return IntMath.ceil(total, this.plainBlock.length) * this.codeBlockLength;
            } else {
                return total / this.plainBlock.length * this.codeBlockLength;
            }
        }

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset,
                int destinationLength) {
            if (!this.ended) {
                this.writeToBuffer(source, sourceOffset, sourceLength);
            }
            final var written = this.buffer.read(destination, destinationOffset, destinationLength);
            if (this.ended) {
                return written;
            } else {
                return StreamUtil.lenof(written);
            }
        }

        private void writeToBuffer(byte[] source, int sourceOffset, int sourceLength) {
            final var plainBlock = this.plainBlock;
            final var plainBlockLength = plainBlock.length;
            final var codeBlockLength = this.codeBlockLength;
            final var isSourceLast = StreamUtil.isLast(sourceLength);
            final var length = StreamUtil.lenof(sourceLength);
            if (!this.ended)
                this.ended = isSourceLast;
            var blockRemaining = this.blockRemaining;
            final var totalLength = length + blockRemaining;
            final var outputBlockCount = isSourceLast
                    ? IntMath.ceil(totalLength, plainBlockLength)
                    : totalLength / plainBlockLength;
            final var segmentLength = outputBlockCount * codeBlockLength;
            final var writer = this.buffer.writer();
            writer.stage(segmentLength);

            var written = 0;
            var read = 0;
            while (true) {
                final var len = Math.min(length - read, plainBlockLength - blockRemaining);
                System.arraycopy(source, sourceOffset + read, plainBlock, blockRemaining, len);
                blockRemaining += len;
                read += len;

                final var remain = blockRemaining < plainBlockLength;
                if (blockRemaining == 0 || !isSourceLast && remain) {
                    break;
                }

                Arrays.fill(plainBlock, blockRemaining, plainBlockLength, (byte) 0);

                System.out.println("plain block:" + dumpHex(plainBlock));
                final var plain = new BigInteger(1, plainBlock);
                final var code = plain.modPow(exponent, modulo);
                // code satisfies 0 <= code < modulo, so bin length is outputBlockLength at most
                final var bin = code.toByteArray();
                System.out.println(" code block:" + dumpHex(bin));
                // bin.length may exceeds inputBlockLength
                final var l = Math.min(codeBlockLength, bin.length);
                final var o = bin.length - l;
                System.arraycopy(bin, o, writer.stagedBuffer(),
                        written + (codeBlockLength - l) + writer.stagedOffset(), l);

                written += codeBlockLength;
                blockRemaining = 0;

                if (isSourceLast && remain) {
                    break;
                }
            }

            this.blockRemaining = blockRemaining;
            writer.finish(segmentLength);
        }

    }

    private static class Decrypter implements Transformer {
        public Decrypter(BigInteger secret, BigInteger modulo) {
            this.secret = secret;
            this.modulo = modulo;
            final var codeBlockLength = codeBlockLength(modulo);
            this.plainBlockLength = plainBlockLength(modulo);
            this.codeBlock = new byte[codeBlockLength];
            this.buffer = new StreamBuffer();
            this.ended = false;
        }

        private final int plainBlockLength;
        private int blockRemaining;
        private final byte[] codeBlock;
        private final BigInteger secret;
        private final BigInteger modulo;
        private final StreamBuffer buffer;
        private boolean ended;

        @Override
        public int maximumOutputSize(int sourceLength) {
            final var len = StreamUtil.lenof(sourceLength);
            final var total = this.blockRemaining + len;
            if (StreamUtil.isLast(sourceLength)) {
                return IntMath.ceil(total, this.codeBlock.length) * this.plainBlockLength;
            } else {
                return total / this.codeBlock.length * this.plainBlockLength;
            }
        }

        @Override
        public int transform(byte[] source, int sourceOffset, int sourceLength, byte[] destination,
                int destinationOffset, int destinationLength) {

            System.out.println("decrypt");
            System.out.println(dumpHex(source, sourceOffset, StreamUtil.lenof(sourceLength)));
            if (!this.ended) {
                this.writeToBuffer(source, sourceOffset, sourceLength);
            }
            final var written = this.buffer.read(destination, destinationOffset, destinationLength);

            if (this.ended) {
                return written;
            } else {
                return StreamUtil.lenof(written);
            }
        }

        private void writeToBuffer(byte[] source, int offset, int sourceLength) {
            final var block = this.codeBlock;
            final var codeBlockLength = block.length;
            final var plainBlockLength = this.plainBlockLength;
            final var isSourceLast = StreamUtil.isLast(sourceLength);
            if (!this.ended)
                this.ended = isSourceLast;
            final var length = StreamUtil.lenof(sourceLength);
            var blockRemaining = this.blockRemaining;
            final var total = length + blockRemaining;
            final var outputLength = isSourceLast
                    ? IntMath.ceil(total, codeBlockLength)
                    : total / codeBlockLength;
            final var segmentLength = outputLength * plainBlockLength;
            final var writer = this.buffer.writer();
            writer.stage(segmentLength);
            var written = 0;
            var read = 0;

            while (true) {
                final var len = Math.min(length - read, codeBlockLength - blockRemaining);
                System.arraycopy(source, offset + read, block, blockRemaining, len);
                blockRemaining += len;
                read += len;
                Arrays.fill(block, blockRemaining, codeBlockLength, (byte) 0);

                final var blockIsNotFull = blockRemaining < codeBlockLength;
                if (blockRemaining == 0 || !isSourceLast && blockIsNotFull) {
                    break;
                }

                System.out.println(" code block:" + dumpHex(block));
                final var code = new BigInteger(1, block);
                final var plain = code.modPow(secret, modulo);
                final var bin = plain.toByteArray();
                System.out.println("plain block:" + dumpHex(bin));
                // bin.length may differ from inputBlockLength

                final var l = Math.min(plainBlockLength, bin.length);
                final var o = bin.length - l;
                System.arraycopy(bin, o, writer.stagedBuffer(),
                        written + (plainBlockLength - l) + writer.stagedOffset(), l);
                written += plainBlockLength;
                blockRemaining = 0;

                if (isSourceLast && blockIsNotFull) {
                    break;
                }
            }
            writer.finish(segmentLength);
            this.blockRemaining = blockRemaining;
        }

    }
}