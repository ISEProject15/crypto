package project.test.unitTests.crypto;

import java.math.BigInteger;
import project.lib.StreamUtil;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestAnnotation;

@TestAnnotation
class RSAPlainChunkedTest {

    @TestAnnotation(order = 0)
    void generateKey_should_return_null_when_parameter_less_than_9() {
        for (var k = -1; k < 9; ++k) {
            assert RSAPlainChunked.generateKey(k) == null
                    : String.format("generateKey returned non null when k = %d", k);
        }
    }

    @TestAnnotation(order = 1)
    void keybundle_modulo_should_not_square() {
        final var bundle = RSAPlainChunked.generateKey(33);
        assert bundle.modulo.sqrt().pow(2) != bundle.modulo;
    }

    @TestAnnotation(order = 2)
    void plain_block_should_less_than_modulo() {
        final var bundle = RSAPlainChunked.generateKey(33);
        final var plainBlockLength = RSAPlainChunked.plainBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(plainBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) > 0;
    }

    @TestAnnotation(order = 3)
    void code_block_should_greater_than_modulo() {
        final var bundle = RSAPlainChunked.generateKey(33);
        final var codeBlockLength = RSAPlainChunked.codeBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(codeBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) < 0;
    }

    @TestAnnotation(order = 4)
    void validate_encrypter() {
        final var bundle = RSAPlainChunked.generateKey(33);
        final var encripter = RSAPlainChunked.encripter(bundle.exponent, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        final var encrypted = new byte[encripter.maximumOutputSize(~plain.length)];
        final var written = encripter.transform(plain, 0, ~plain.length, encrypted, 0, encrypted.length);
        System.out.println("written: " + written);
        System.out.println(BinaryDebug.dumpHex(encrypted));
        assert written < 0 : "maximumOutputSize " + encrypted.length + " should greater than output";
        assert StreamUtil.lenof(written) % RSAPlainChunked.codeBlockLength(bundle.modulo) == 0
                : "encrypted binary length should multiple of code block length";
    }

    @TestAnnotation(order = 5)
    void validate_decrypter() {
        final var bundle = RSAPlainChunked.generateKey(33);
        final var encripter = RSAPlainChunked.encripter(bundle.exponent, bundle.modulo);
        final var decripter = RSAPlainChunked.decripter(bundle.secret, bundle.modulo);
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, };
        final var enc = new byte[encripter.maximumOutputSize(~plain.length)];
        final var encWritten = encripter.transform(plain, 0, ~plain.length, enc, 0, enc.length);
        final var dec = new byte[decripter.maximumOutputSize(StreamUtil.lenof(encWritten))];
        final var decWritten = decripter.transform(enc, 0, ~StreamUtil.lenof(encWritten), dec,
                0, dec.length);

        System.out.println("encrypted written: " + encWritten);
        System.out.println(BinaryDebug.dumpHex(enc));
        System.out.println("decrypted written: " + decWritten);
        System.out.println(BinaryDebug.dumpHex(dec));

        assert decWritten < 0 : "maximumOutputSize " + enc.length + " should greater than output";
        assert StreamUtil.lenof(decWritten) % RSAPlainChunked.plainBlockLength(bundle.modulo) == 0
                : "decrypted binary length should multiple of plain block length";

        for (var i = 0; i < Math.min(plain.length, StreamUtil.lenof(decWritten)); ++i) {
            assert plain[i] == dec[i] : "decryption failed";
        }

        System.out.println(BinaryDebug.dumpHexDiff(plain, dec));
    }
}
