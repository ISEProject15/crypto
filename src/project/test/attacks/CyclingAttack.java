package project.test.attacks;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Function;

import project.lib.crypto.algorithm.*;
import project.scaffolding.IndentedAppendable;

public class CyclingAttack {
    public static void demo() throws Exception {
        final var writer = IndentedAppendable.create(System.out, "  ");
        final var sampleCount = 1024;
        final var kMax = 10;
        final var random = new Random();

        final var summaryFile = new File("src/project/test/artifacts/cycling_attack.csv");
        summaryFile.createNewFile();
        final var summaryStream = new FileWriter(summaryFile, false);

        for (var k = 3; k < kMax; ++k) {
            for (var i = 0; i < sampleCount; ++i) {
                final var keybundle = RSA.generateKey(k, random);
                final var plainLength = RSA.bitPlainBlockLength(keybundle.modulo);
                final var plain = new BigInteger(plainLength, random);
                final var encrypter = encrypter(keybundle.exponent, keybundle.modulo);
                final var code = encrypter.apply(plain);
                writer.indentLevel(0);
                writer.println("keypair: " + keybundle).indent();
                writer.println("plain: " + plain);
                writer.println("code: " + code);

                final var cycle = calcCycle(code, encrypter);

                writer.println("cycle: " + cycle);
                final var decoded = nthApply(cycle - 1, code, encrypter);

                writer.println("decoded: " + decoded);
                writer.println("decoded == plain: " + (plain.equals(decoded)));

                summaryStream.write(String.valueOf(k));
                summaryStream.write(",");
                summaryStream.write(keybundle.modulo.toString());
                summaryStream.write(",");
                summaryStream.write(String.valueOf(cycle));
                summaryStream.write("\r\n");
            }
        }
        summaryStream.flush();
        summaryStream.close();
    }

    private static Function<BigInteger, BigInteger> encrypter(BigInteger exponent, BigInteger modulo) {
        return (plain) -> plain.modPow(exponent, modulo);
    }

    private static <T> T nthApply(long n, T seed, Function<T, T> f) {
        for (var i = 0L; i < n; ++i) {
            seed = f.apply(seed);
        }
        return seed;
    }

    private static long calcCycle(BigInteger code, Function<BigInteger, BigInteger> encrypter) {
        var c = encrypter.apply(code);
        for (var i = 1L;; ++i) {
            if (c.equals(code)) {
                return i;
            }
            c = encrypter.apply(c);
        }
    }
}
