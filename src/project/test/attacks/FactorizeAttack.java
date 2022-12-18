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
import project.scaffolding.debug.IndentedAppendable;
import project.test.scaffolding.DoubleRandomAccess;
import project.test.scaffolding.benchmark.BenchmarkServer;
import project.test.scaffolding.benchmark.BenchmarkSummary;
import project.test.scaffolding.drawing.Graph2SvgEncoder;
import project.test.scaffolding.statistics.Statistic;

public class FactorizeAttack {
    public static void demo() throws Exception {
        final var random = new Random();
        final var sampleCount = 512;
        final var kMax = 22;
        final var perKsamplings = new ArrayList<BenchmarkSummary[]>(kMax);

        for (var k = 3; k < kMax; ++k) {
            final var samplings = new BenchmarkSummary[sampleCount];
            for (var i = 0; i < sampleCount; ++i) {
                BenchmarkServer.initialize();
                final var keyBundle = RSA.generateKey(k, random);
                final var facts = factorize(keyBundle.modulo);
                final var summary = BenchmarkServer.terminate();
                samplings[i] = summary;
                System.out.print(keyBundle.modulo);
                System.out.print(" ");
                System.out.print(summary.totalProgramCount());
                System.out.print(" ");
                System.out.println(facts);
            }
            perKsamplings.add(samplings);
        }

        final var statistic = new Statistic(3);

        final var summary = statistic.analyze(DoubleRandomAccess.from(perKsamplings, s -> s.size(), (s, i) -> {
            final var samplings = s.get(i);
            final var sum = statistic
                    .analyze(DoubleRandomAccess.from(samplings, t -> t.length, (t, idx) -> t[idx].totalProgramCount()));
            return sum.mean();
        }));

        for (final var measure : summary.measures()) {
            System.out.println(measure);
        }

        final var graph = summary.print();
        final var file = new File("src\\project\\test\\artifacts\\factorize_attack.svg");
        file.createNewFile();
        final var stream = new FileWriter(file, false);
        final var builder = IndentedAppendable.create(stream, "  ");
        final var encoder = new Graph2SvgEncoder(1000, 1000);
        encoder.encode(graph, builder);
        stream.flush();

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