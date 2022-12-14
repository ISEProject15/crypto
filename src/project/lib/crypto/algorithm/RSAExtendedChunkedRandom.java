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

public final class RSAExtendedChunkedRandom {
    private RSAExtendedChunkedRandom() {

    }

    public static RSAKeyBundle generateKey(int k, Random random) {
        return RSAExtendedChunked.generateKey(k, random);
    }

    public static Transformer<byte[]> encrypter(BigInteger exponent, BigInteger modulo, Random random) {
        return new Encrypter(exponent, modulo, random);
    }

    public static Transformer<byte[]> decrypter(BigInteger secret, BigInteger modulo) {
        return RSAExtendedChunked.decrypter(secret, modulo);
    }

    private static final class Encrypter extends TransformerBase<byte[]> {
        Encrypter(BigInteger exponent, BigInteger modulo, Random random) {
            super(ByteArrayPool.instance());
            final var plainBlockLength = RSA.plainBlockLength(modulo) - 1;
            this.plainBlockLength = plainBlockLength;
            this.codeBlockLength = RSA.codeBlockLength(modulo);
            this.exponent = exponent;
            this.modulo = modulo;
            this.random = random;
            this.subject.listen(BlockBufferWriterListenerProxy.wrap(new byte[plainBlockLength], this::transform));
        }

        private final int plainBlockLength;
        private final int codeBlockLength;
        private final BigInteger exponent;
        private final BigInteger modulo;
        private final Random random;

        private void transform(byte[] buffer, int offset, int length) {
            final var codeBlockLength = this.codeBlockLength;
            final var plain = new BigInteger(1, buffer, offset, this.plainBlockLength).or(this.extension());
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

        private BigInteger extension() {
            final var rnd = new BigInteger(7, random);
            return rnd.setBit(7).shiftLeft(plainBlockLength * 8);
        }
    }
}
