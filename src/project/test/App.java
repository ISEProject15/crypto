package project.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import project.lib.crypto.algorithm.*;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.SequenceFunnel;
import project.lib.scaffolding.streaming.StreamBuffer;
import project.scaffolding.debug.BinaryDebug;
import project.scaffolding.debug.IndentedAppendable;
import project.test.scaffolding.ProgressiveStatistic;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;

public class App {
    public static void main(String[] args) throws Exception {
        // final var tests = TestCollector.collect("project.test.unitTests");
        // TestExecutor.execute(TestExecutorOptions.standard(), tests);

        // final var method = App.class.getDeclaredMethod("sample");
        final var file = new File("src\\project\\test\\artifacts\\graph.svg");
        file.createNewFile();
        final var stream = new FileWriter(file, false);
        final var random = new Random();
        final var num = BigInteger.valueOf(167);
        System.err.println(sample(num).toString());

        final var statistic = statistic(() -> benchmark(() -> null), 1 << 14);
        final var graph = statistic.printGraph();
        final var builder = IndentedAppendable.create(stream, "  ");
        graph.encode(builder);
        System.out.println(statistic.sampleCount());
        System.out.println(statistic.mean());
        stream.close();
    }

    static ProgressiveStatistic statistic(LongSupplier benchmark, int count) {
        final var statistic = new ProgressiveStatistic(3, count);
        for (var i = 0; i < count; ++i) {
            final var measure = benchmark.getAsLong();
            statistic.add(measure);
        }
        statistic.clear();
        for (var i = 0; i < count; ++i) {
            final var measure = benchmark.getAsLong();
            statistic.add(measure);
        }
        return statistic;
    }

    static <T> long benchmark(Callable<T> function) {
        try {
            final var startTime = System.nanoTime();
            function.call();
            final var endTime = System.nanoTime();
            return endTime - startTime;
        } catch (Exception e) {
            return -1;
        }
    }

    static Set<Entry<BigInteger, Integer>> sample(BigInteger num) {
        if (num.equals(BigInteger.ZERO)) {
            return Collections.emptySet();
        }

        final var hash = new HashMap<BigInteger, Integer>();

        final var lsb = num.getLowestSetBit();
        if (lsb > 0) {
            hash.put(BigInteger.TWO, lsb);
            num = num.shiftRight(lsb);
        }
        var div = BigInteger.valueOf(3);

        while (!num.equals(BigInteger.ONE)) {
            var exp = 0;
            while (num.remainder(div).equals(BigInteger.ZERO)) {
                num = num.divide(div);
                exp++;
            }
            if (exp > 0) {
                hash.put(div, exp);
            }
            div = div.add(BigInteger.TWO);
        }

        return hash.entrySet();
    }
}