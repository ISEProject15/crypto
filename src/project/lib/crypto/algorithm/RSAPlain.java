package project.lib.crypto.algorithm;

import java.math.BigInteger;

import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.streaming.BlockTransformerBase;
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

    private static class Encrypter extends TransformerBase<byte[]> {
        protected Encrypter(BigInteger exponent, BigInteger modulo) {
            super(ByteArrayPool.instance());
        }

        protected final void transform(byte[] buffer, int offset, int length) {

        }
    }

    private static class Decrypter extends BlockTransformerBase<byte[]> {

        protected Decrypter(BigInteger secret, BigInteger modulo) {
            super(RSA.codeBlockLength(modulo), ByteArrayPool.instance());
        }

        @Override
        protected void transform(byte[] buffer, int offset, boolean isLast) {

        }
    }
}
