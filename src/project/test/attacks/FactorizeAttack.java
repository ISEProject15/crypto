package project.test.attacks;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import project.lib.crypto.algorithm.RSA;
import project.test.scaffolding.benchmark.BenchmarkServer;
import project.test.scaffolding.benchmark.BenchmarkSummary;

public class FactorizeAttack {
    public static void demo() throws Exception {
        final var random = new Random();
        final var sampleCount = 512;
        final var kMax = 22;
        final var perKsamplings = new ArrayList<BenchmarkSummary[]>(kMax);

        final var file = new File("src\\project\\test\\artifacts\\factorize_attack.csv");
        file.createNewFile();
        final var stream = new FileWriter(file, false);

        for (var k = 3; k < kMax; ++k) {
            final var samplings = new BenchmarkSummary[sampleCount];
            for (var i = 0; i < sampleCount; ++i) {
                BenchmarkServer.initialize();
                final var keyBundle = RSA.generateKey(k, random);
                final var facts = factorize(keyBundle.modulo);
                final var summary = BenchmarkServer.terminate();
                samplings[i] = summary;

                stream.write(String.valueOf(k));
                stream.write(",");
                stream.write(String.valueOf(summary.totalProgramCount()));
                stream.write("\r\n");

                System.out.print(keyBundle.modulo);
                System.out.print(" ");
                System.out.print(summary.totalProgramCount());
                System.out.print(" ");
                System.out.println(facts);
            }
            perKsamplings.add(samplings);
        }

        stream.flush();
        stream.close();
    }

    static Set<Entry<BigInteger, Integer>> factorize(BigInteger num) {
        BenchmarkServer.enter();
        if (num.equals(BigInteger.ZERO)) {
            return Collections.emptySet();
        }

        final var hash = new HashMap<BigInteger, Integer>();

        final var lsb = num.getLowestSetBit();
        if (lsb > 0) {
            hash.put(BigInteger.TWO, lsb);
            BenchmarkServer.increment();
            num = num.shiftRight(lsb);
        }
        var div = BigInteger.valueOf(3);

        while (!num.equals(BigInteger.ONE)) {
            BenchmarkServer.increment();
            var exp = 0;
            while (num.remainder(div).equals(BigInteger.ZERO)) {
                BenchmarkServer.increment();
                num = num.divide(div);
                exp++;
            }
            if (exp > 0) {
                hash.put(div, exp);
            }
            div = div.add(BigInteger.TWO);
        }
        BenchmarkServer.leave();
        return hash.entrySet();
    }
}