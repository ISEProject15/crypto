package project.test.attacks;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import project.lib.crypto.algorithm.RSA;
import project.scaffolding.debug.IndentedAppendable;
import project.test.scaffolding.DoubleRandomAccess;
import project.test.scaffolding.benchmark.BenchmarkServer;
import project.test.scaffolding.benchmark.BenchmarkSummary;
import project.test.scaffolding.drawing.Graph2SvgEncoder;
import project.test.scaffolding.statistics.Statistic;

public class FermatFractorizeAttack {
    public static void demo() throws Exception {
        final var random = new Random();
        final var sampleCount = 512;
        final var kMax = 22;
        final var perDeltaSamplings = new TreeMap<BigInteger, ArrayList<BenchmarkSummary>>();

        final var perKsamplings = new ArrayList<BenchmarkSummary[]>(kMax);

        for (var k = 3; k < kMax; ++k) {
            final var samplings = new BenchmarkSummary[sampleCount];
            for (var i = 0; i < sampleCount; ++i) {
                BenchmarkServer.initialize();
                final var keyBundle = RSA.generateKey(k, random);
                final var facts = fermat(keyBundle.modulo);
                final var summary = BenchmarkServer.terminate();
                samplings[i] = summary;
                final var delta = facts[0].subtract(facts[1]).abs();
                var deltaSamplings = perDeltaSamplings.get(delta);
                if (deltaSamplings == null) {
                    deltaSamplings = new ArrayList<>();
                    perDeltaSamplings.put(delta, deltaSamplings);
                }
                deltaSamplings.add(summary);

                System.out.print(keyBundle.modulo);
                System.out.print(" ");
                System.out.print(summary.totalProgramCount());
                System.out.print(" ");
                System.out.println(Arrays.toString(facts));
            }
            perKsamplings.add(samplings);
        }

        final var statistic = new Statistic(3);

        final var perKsummary = statistic.analyze(DoubleRandomAccess.from(perKsamplings, s -> s.size(), (s, i) -> {
            final var samplings = s.get(i);
            final var sum = statistic
                    .analyze(DoubleRandomAccess.from(samplings, t -> t.length, (t, idx) -> t[idx].totalProgramCount()));
            return sum.mean();
        }));

        final var perKgraph = perKsummary.print();
        final var perKfile = new File("src\\project\\test\\artifacts\\fermat_per_k.svg");
        perKfile.createNewFile();
        final var perKstream = new FileWriter(perKfile, false);
        final var perKbuilder = IndentedAppendable.create(perKstream, "  ");
        final var encoder = new Graph2SvgEncoder(1000, 1000);
        encoder.encode(perKgraph, perKbuilder);
        perKstream.flush();

        final var perDeltasummary = statistic
                .analyze(DoubleRandomAccess.from(perDeltaSamplings.values().toArray(), s -> s.length, (s, i) -> {
                    @SuppressWarnings("unchecked")
                    final var samplings = (ArrayList<BenchmarkSummary>) s[i];
                    final var sum = statistic
                            .analyze(DoubleRandomAccess.from(samplings, t -> t.size(),
                                    (t, idx) -> t.get(idx).totalProgramCount()));
                    return sum.mean();
                }));

        final var perDeltagraph = perDeltasummary.print();
        final var perDeltafile = new File("src\\project\\test\\artifacts\\fermat_per_delta.svg");
        perDeltafile.createNewFile();
        final var perDeltastream = new FileWriter(perDeltafile, false);
        final var perDeltabuilder = IndentedAppendable.create(perDeltastream, "  ");
        encoder.encode(perDeltagraph, perDeltabuilder);
        perDeltastream.flush();
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
