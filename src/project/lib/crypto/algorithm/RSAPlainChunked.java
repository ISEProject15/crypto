package project.lib.crypto.algorithm;

import java.math.BigInteger;

import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.streaming.BlockTransformerBase;
import project.lib.scaffolding.streaming.StreamUtil;
import project.lib.scaffolding.streaming.Transformer;

public class RSAPlainChunked {
    public static Transformer<byte[]> encrypter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer<byte[]> decrypter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static class Encrypter extends BlockTransformerBase<byte[]> {
        Encrypter(BigInteger exponent, BigInteger modulo) {
            super(RSA.plainBlockLength(modulo), ByteArrayPool.instance());
            this.codeBlockLength = RSA.codeBlockLength(modulo);
            this.exponent = exponent;
            this.modulo = modulo;
        }

        private final int codeBlockLength;
        private final BigInteger exponent;
        private final BigInteger modulo;

        @Override
        protected void transform(byte[] buffer, int offset, int length) {
            assert StreamUtil.lenof(length) == this.blockLength;
            final var codeBlockLength = this.codeBlockLength;
            final var plain = new BigInteger(1, buffer, offset, StreamUtil.lenof(length));
            final var code = plain.modPow(this.exponent, this.modulo);
            final var bin = code.toByteArray();
            final var writer = this.buffer.writer();

            writer.stage(codeBlockLength);
            final var len = Math.min(codeBlockLength, bin.length);
            final var off = bin.length - len;
            System.arraycopy(bin, off, writer.stagedBuffer(), writer.stagedOffset() + codeBlockLength - len, len);
            if (StreamUtil.isLast(length)) {
                writer.finish(~codeBlockLength);
                this.markCompleted();
            } else {
                writer.finish(codeBlockLength);
            }
        }
    }

    private static class Decrypter extends BlockTransformerBase<byte[]> {
        Decrypter(BigInteger secret, BigInteger modulo) {
            super(RSA.codeBlockLength(modulo), ByteArrayPool.instance());
            this.plainBlockLength = RSA.plainBlockLength(modulo);
            this.secret = secret;
            this.modulo = modulo;
        }

        private final int plainBlockLength;
        private final BigInteger secret;
        private final BigInteger modulo;

        @Override
        protected void transform(byte[] buffer, int offset, int length) {
            assert StreamUtil.lenof(length) == this.blockLength;
            final var plainBlockLength = this.plainBlockLength;
            final var code = new BigInteger(1, buffer, offset, StreamUtil.lenof(length));
            final var plain = code.modPow(this.secret, this.modulo);
            final var bin = plain.toByteArray();
            final var writer = this.buffer.writer();

            writer.stage(plainBlockLength);
            final var len = Math.min(plainBlockLength, bin.length);
            final var off = bin.length - len;
            System.arraycopy(bin, off, writer.stagedBuffer(), writer.stagedOffset() + plainBlockLength - len, len);
            if (StreamUtil.isLast(length)) {
                writer.finish(~plainBlockLength);
                this.markCompleted();
            } else {
                writer.finish(plainBlockLength);
            }
        }
    }
}
