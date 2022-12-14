package project.lib.crypto.algorithm;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.RSAKeyBundle;
import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.streaming.BlockBufferWriterListenerProxy;
import project.lib.scaffolding.streaming.StreamUtil;
import project.lib.scaffolding.streaming.Transformer;
import project.lib.scaffolding.streaming.TransformerBase;

public final class RSAExtendedChunked {
    private RSAExtendedChunked() {

    }

    public static RSAKeyBundle generateKey(int k, Random random) {
        if (k <= 16) {
            return null;
        }
        return RSA.generateKey(k, random);
    }

    public static Transformer<byte[]> encrypter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo, (byte) 0xFF);
    }

    public static Transformer<byte[]> decrypter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static final class Encrypter extends TransformerBase<byte[]> {
        Encrypter(BigInteger exponent, BigInteger modulo, byte extension) {
            super(ByteArrayPool.instance());
            final var plainBlockLength = RSA.plainBlockLength(modulo) - 1;
            this.plainBlockLength = plainBlockLength;
            this.codeBlockLength = RSA.codeBlockLength(modulo);
            this.exponent = exponent;
            this.modulo = modulo;
            this.extension = BigInteger.valueOf(Byte.toUnsignedLong(extension)).shiftLeft(plainBlockLength * 8);
            this.subject.listen(BlockBufferWriterListenerProxy.wrap(new byte[plainBlockLength], this::transform));
        }

        private final int plainBlockLength;
        private final int codeBlockLength;
        private final BigInteger exponent;
        private final BigInteger modulo;
        private final BigInteger extension;

        private void transform(byte[] buffer, int offset, int length) {
            final var codeBlockLength = this.codeBlockLength;
            final var plain = new BigInteger(1, buffer, offset, this.plainBlockLength).or(this.extension);
            final var code = plain.modPow(this.exponent, this.modulo);
            final var bin = code.toByteArray();
            final var writer = this.buffer.writer();

            writer.stage(codeBlockLength);
            ArrayUtil.copyFromBack2Back(bin, writer.stagedBuffer(), writer.stagedOffset(), codeBlockLength);
            if (StreamUtil.isLast(length)) {
                writer.finish(~codeBlockLength);
                this.markCompleted();
            } else {
                writer.finish(codeBlockLength);
            }
        }
    }

    private static final class Decrypter extends TransformerBase<byte[]> {
        Decrypter(BigInteger secret, BigInteger modulo) {
            super(ByteArrayPool.instance());
            final var codeBlockLength = RSA.codeBlockLength(modulo);
            this.codeBlockLength = codeBlockLength;
            this.plainBlockLength = RSA.plainBlockLength(modulo) - 1;
            this.secret = secret;
            this.modulo = modulo;

            this.subject.listen(BlockBufferWriterListenerProxy.wrap(new byte[codeBlockLength], this::transform));
        }

        private final int codeBlockLength;
        private final int plainBlockLength;
        private final BigInteger secret;
        private final BigInteger modulo;

        private void transform(byte[] buffer, int offset, int length) {
            final var plainBlockLength = this.plainBlockLength;
            final var code = new BigInteger(1, buffer, offset, this.codeBlockLength);
            final var plain = code.modPow(this.secret, this.modulo);
            final var bin = plain.toByteArray();
            final var writer = this.buffer.writer();
            writer.stage(plainBlockLength);
            ArrayUtil.copyFromBack2Back(bin, writer.stagedBuffer(), writer.stagedOffset(), plainBlockLength);
            if (StreamUtil.isLast(length)) {
                writer.finish(~plainBlockLength);
                this.markCompleted();
            } else {
                writer.finish(plainBlockLength);
            }
        }
    }
}
