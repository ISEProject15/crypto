package project.test.unitTests.crypto;

import java.util.Random;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.lib.scaffolding.streaming.SequenceStreamReader;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.testing.TestAnnotation;

@TestAnnotation
class RSAPlainChunkedTest {
    @TestAnnotation(order = 0)
    void validate_encrypter() {
        final var random = new Random();
        final var bundle = RSAPlainChunked.generateKey(5, random);
        final var encrypter = RSAPlainChunked.encrypter(bundle.exponent, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));
        assert encrypted.length() % RSA.codeBlockLength(bundle.modulo) == 0
                : "encrypted binary length should multiple of code block length";
    }

    @TestAnnotation(order = 1)
    void validate_decrypter() throws Exception {
        final var random = new Random();
        final var bundle = RSAPlainChunked.generateKey(5, random);
        final var encrypter = RSAPlainChunked.encrypter(bundle.exponent, bundle.modulo);
        final var decrypter = RSAPlainChunked.decrypter(bundle.secret, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));
        final var decryptedSeq = SequenceStreamReader.from(encrypted).transform(decrypter).readTotally();
        System.out.println(BinaryDebug.dumpHex(decryptedSeq));
        final var decrypted = decryptedSeq.toArray();

        assert decrypted.length % RSA.plainBlockLength(bundle.modulo) == 0
                : "decrypted binary length should multiple of plain block length";

        for (var i = 0; i < plain.length; ++i) {
            assert plain[i] == decrypted[i] : "decryption failed";
        }

        System.out.println(BinaryDebug.dumpHexDiff(plain, decrypted));
    }
}
