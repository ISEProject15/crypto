package project.test.unitTests.crypto;

import java.math.BigInteger;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.lib.scaffolding.streaming.StreamUtil;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestAnnotation;

@TestAnnotation
class RSAPlainChunkedTest {

    @TestAnnotation
    void validate_encrypter() {
        final var bundle = RSA.generateKey(33);
        final var encrypter = RSAPlainChunked.encrypter(bundle.exponent, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));
        assert encrypted.length() % RSA.codeBlockLength(bundle.modulo) == 0
                : "encrypted binary length should multiple of code block length";
    }

    @TestAnnotation(order = 5)
    void validate_decrypter() {
        final var bundle = RSA.generateKey(33);
        final var encrypter = RSAPlainChunked.encrypter(bundle.exponent, bundle.modulo);
        final var decrypter = RSAPlainChunked.decrypter(bundle.secret, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));

        assert decWritten < 0 : "maximumOutputSize " + enc.length + " should greater than output";
        assert StreamUtil.lenof(decWritten) % RSA.plainBlockLength(bundle.modulo) == 0
                : "decrypted binary length should multiple of plain block length";

        for (var i = 0; i < Math.min(plain.length, StreamUtil.lenof(decWritten)); ++i) {
            assert plain[i] == dec[i] : "decryption failed";
        }

        System.out.println(BinaryDebug.dumpHexDiff(plain, dec));
    }
}
