package project.test.attacks;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import project.lib.crypto.algorithm.RSA;
import project.test.scaffolding.benchmark.BenchmarkServer;
import project.test.scaffolding.benchmark.BenchmarkSummary;

public class FermatFractorizeAttack {
    public static void demo() throws Exception {
        final var random = new Random();
        final var sampleCount = 512;
        final var kMax = 22;
        final var summaryFile = new File("src\\project\\test\\artifacts\\fermat_attack.csv");
        summaryFile.createNewFile();
        final var summaryStream = new FileWriter(summaryFile, false);

        for (var k = 3; k < kMax; ++k) {
            final var samplings = new BenchmarkSummary[sampleCount];
            for (var i = 0; i < sampleCount; ++i) {
                BenchmarkServer.initialize();
                final var keyBundle = RSA.generateKey(k, random);
                final var facts = fermat(keyBundle.modulo);
                final var summary = BenchmarkServer.terminate();
                samplings[i] = summary;
                final var delta = facts[0].subtract(facts[1]).abs();

                summaryStream.write(String.valueOf(k));
                summaryStream.write(", ");
                summaryStream.write(delta.toString());
                summaryStream.write(", ");
                summaryStream.write(String.valueOf(summary.totalProgramCount()));
                summaryStream.write("\r\n");

                System.out.print(keyBundle.modulo);
                System.out.print(" ");
                System.out.print(summary.totalProgramCount());
                System.out.print(" ");
                System.out.println(Arrays.toString(facts));
            }

        }

        summaryStream.flush();
        summaryStream.close();
    }

    private static final int[] mod32Table = new int[] { 0, 1, 4, 9, 16, 17, 25 };
    private static final BigInteger i31 = BigInteger.valueOf(31);

    private static BigInteger[] fermat(BigInteger modulo) {
        BenchmarkServer.enter();
        final var fourModulo = modulo.multiply(BigInteger.valueOf(4));

        var p_minus_q = BigInteger.TWO;
        BigInteger p_plus_q = null;
        while (true) {
            final var num = fourModulo.add(p_minus_q.multiply(p_minus_q));
            p_plus_q = trySqrt(num);
            BenchmarkServer.increment();
            if (p_plus_q != null) {
                break;
            }
            p_minus_q = p_minus_q.add(BigInteger.TWO);
        }

        final var p = p_plus_q.add(p_minus_q).divide(BigInteger.TWO);
        final var q = p_plus_q.subtract(p_minus_q).divide(BigInteger.TWO);
        BenchmarkServer.leave();

        return new BigInteger[] { p, q };
    }

    private static BigInteger trySqrt(BigInteger num) {
        final var modi32 = num.and(i31).intValue();
        if (Arrays.binarySearch(mod32Table, modi32) < 0) {
            return null;
        }
        final var sqrt = num.sqrt();
        if (sqrt.multiply(sqrt).equals(num)) {
            return sqrt;
        }
        return null;
    }
}
