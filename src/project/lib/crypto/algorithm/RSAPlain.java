package project.lib.crypto.algorithm;

import java.math.BigInteger;
import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.streaming.BlockBufferWriterListenerProxy;
import project.lib.scaffolding.streaming.StreamUtil;
import project.lib.scaffolding.streaming.Transformer;
import project.lib.scaffolding.streaming.TransformerBase;

public final class RSAPlain {
    private RSAPlain() {

    }

    public static Transformer<byte[]> encrypter(BigInteger exponent, BigInteger modulo) {
        return new Encrypter(exponent, modulo);
    }

    public static Transformer<byte[]> decrypter(BigInteger secret, BigInteger modulo) {
        return new Decrypter(secret, modulo);
    }

    private static final class Encrypter extends TransformerBase<byte[]> {
        protected Encrypter(BigInteger exponent, BigInteger modulo) {
            super(ByteArrayPool.instance());
            this.exponent = exponent;
            this.modulo = modulo;
            this.codeBlockLength = RSA.codeBlockLength(modulo);
            this.subject.listen(this::transform);
        }

        private final BigInteger exponent, modulo;
        private final int codeBlockLength;

        private final void transform(byte[] buffer, int offset, int length) {
            final var codeBlockLength = this.codeBlockLength;
            final var exponent = this.exponent;
            final var modulo = this.modulo;
            final var writer = this.buffer.writer();
            writer.stage(StreamUtil.lenof(length) * codeBlockLength);
            final var lastBound = offset + StreamUtil.lenof(length);
            var written = 0;
            for (var i = offset; i < lastBound; ++i) {
                final var plain = new BigInteger(1, buffer, i, 1);
                final var code = plain.modPow(exponent, modulo);
                final var bin = code.toByteArray();
                ArrayUtil.copyFromBack2Back(bin, writer.stagedBuffer(),
                        writer.stagedOffset() + written, codeBlockLength);
                written += codeBlockLength;
            }
            writer.finish(written ^ StreamUtil.flagof(length));

            if (StreamUtil.isLast(length)) {
                this.markCompleted();
            }
        }
    }

    private static final class Decrypter extends TransformerBase<byte[]> {
        protected Decrypter(BigInteger secret, BigInteger modulo) {
            super(ByteArrayPool.instance());
            final var codeBlockLength = RSA.codeBlockLength(modulo);
            this.codeBlockLength = codeBlockLength;
            this.secret = secret;
            this.modulo = modulo;
            this.subject.listen(BlockBufferWriterListenerProxy.wrap(new byte[codeBlockLength], this::transform));
        }

        private final BigInteger secret, modulo;
        private final int codeBlockLength;

        private void transform(byte[] buffer, int offset, int length) {
            final var code = new BigInteger(1, buffer, offset, this.codeBlockLength);
            final var plain = code.modPow(this.secret, this.modulo);
            final var bin = plain.toByteArray();
            final var writer = this.buffer.writer();

            writer.stage(1);
            ArrayUtil.copyFromBack2Back(bin, writer.stagedBuffer(), writer.stagedOffset(), 1);
            if (StreamUtil.isLast(length)) {
                writer.finish(~1);
                this.markCompleted();
            } else {
                writer.finish(1);
            }
        }
    }
}
