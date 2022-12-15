package project.test.unitTests.crypto;

import java.util.Random;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAExtendedChunked;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.SequenceFunnel;
import project.lib.scaffolding.streaming.BlockBufferWriterListenerProxy;
import project.lib.scaffolding.streaming.SequenceStreamReader;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestAnnotation;

@TestAnnotation
public class RSAExtendedChunkedTest {
    @TestAnnotation(order = 0)
    void encrypter_should_not_output_weak_plain() {
        final var random = new Random();
        final var bundle = RSAExtendedChunked.generateKey(9, random);
        final var encrypter = RSAExtendedChunked.encrypter(bundle.exponent, bundle.modulo);
        final var plain = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        final var zeroed = new byte[RSA.codeBlockLength(bundle.modulo)];
        SequenceFunnel.into(encrypted,
                BlockBufferWriterListenerProxy.wrap(new byte[RSA.codeBlockLength(bundle.modulo)],
                        (buffer, offset, length) -> {
                            final var c = ArrayUtil.compare(buffer, offset, offset + length, zeroed, 0, zeroed.length);
                            assert c != 0;
                        }));
    }

    @TestAnnotation(order = 1)
    void encrypter_output_length_should_multiple_of_code_block_length() {
        final var random = new Random();
        final var bundle = RSAExtendedChunked.generateKey(9, random);
        final var encrypter = RSAExtendedChunked.encrypter(bundle.exponent, bundle.modulo);
        final var plain = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));
        assert encrypted.length() % RSA.codeBlockLength(bundle.modulo) == 0
                : "encrypted binary length should multiple of code block length";

    }

    @TestAnnotation(order = 2)
    void decrypter_output_length_should_multiple_of_plain_block_length() throws Exception {
        final var random = new Random();
        final var bundle = RSAExtendedChunked.generateKey(9, random);
        final var encrypter = RSAExtendedChunked.encrypter(bundle.exponent, bundle.modulo);
        final var decrypter = RSAExtendedChunked.decrypter(bundle.secret, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));

        final var decrypted = SequenceStreamReader.from(encrypted).transform(decrypter).readTotally().toArray();

        assert decrypted.length % RSA.plainBlockLength(bundle.modulo) == 0
                : "decrypted binary length should multiple of plain block length";
    }

    @TestAnnotation(order = 3)
    void decrypter_output_should_equal_to_plain() throws Exception {
        final var random = new Random();
        final var bundle = RSAExtendedChunked.generateKey(9, random);
        final var encrypter = RSAExtendedChunked.encrypter(bundle.exponent, bundle.modulo);
        final var decrypter = RSAExtendedChunked.decrypter(bundle.secret, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        encrypter.writer().write(plain, 0, ~plain.length);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));

        final var decrypted = SequenceStreamReader.from(encrypted).transform(decrypter).readTotally().toArray();
        for (var i = 0; i < plain.length; ++i) {
            assert plain[i] == decrypted[i] : "decryption failed";
        }

        System.out.println(BinaryDebug.dumpHexDiff(plain, decrypted));
    }
}
