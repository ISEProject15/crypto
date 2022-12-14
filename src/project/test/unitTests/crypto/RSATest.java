package project.test.unitTests.crypto;

import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.algorithm.RSA;
import project.test.scaffolding.TestAnnotation;

@TestAnnotation
public class RSATest {
    @TestAnnotation
    void keybundle_modulo_should_not_square() {
        final var random = new Random();
        final var bundle = RSA.generateKey(33, random);
        assert bundle.modulo.sqrt().pow(2) != bundle.modulo;
    }

    @TestAnnotation
    void plain_block_should_less_than_modulo() {
        final var random = new Random();
        final var bundle = RSA.generateKey(33, random);
        final var plainBlockLength = RSA.plainBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(plainBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) > 0;
    }

    @TestAnnotation
    void code_block_should_greater_than_modulo() {
        final var random = new Random();
        final var bundle = RSA.generateKey(33, random);
        final var codeBlockLength = RSA.codeBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(codeBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) < 0;
    }

}
