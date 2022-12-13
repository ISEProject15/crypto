package project.test.unitTests.crypto;

import java.math.BigInteger;

import project.lib.crypto.algorithm.RSA;
import project.test.scaffolding.TestAnnotation;

@TestAnnotation
public class RSATest {
    @TestAnnotation
    void generateKey_should_return_null_when_parameter_less_than_9() {
        for (var k = -1; k < 9; ++k) {
            assert RSA.generateKey(k) == null
                    : String.format("generateKey returned non null when k = %d", k);
        }
    }

    @TestAnnotation
    void keybundle_modulo_should_not_square() {
        final var bundle = RSA.generateKey(33);
        assert bundle.modulo.sqrt().pow(2) != bundle.modulo;
    }

    @TestAnnotation
    void plain_block_should_less_than_modulo() {
        final var bundle = RSA.generateKey(33);
        final var plainBlockLength = RSA.plainBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(plainBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) > 0;
    }

    @TestAnnotation
    void code_block_should_greater_than_modulo() {
        final var bundle = RSA.generateKey(33);
        final var codeBlockLength = RSA.codeBlockLength(bundle.modulo);
        // 2 ^ (plainBlockLength * 8) - 1
        final var max = BigInteger.ONE.shiftLeft(codeBlockLength * 8).subtract(BigInteger.ONE);
        assert bundle.modulo.compareTo(max) < 0;
    }

}
