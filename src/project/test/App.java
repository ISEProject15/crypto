package project.test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import project.lib.crypto.algorithm.*;
import project.lib.scaffolding.collections.SequenceFunnel;
import project.lib.scaffolding.streaming.StreamBuffer;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;

public class App {
    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.verbose(), tests);

        final var method = App.class.getDeclaredMethod("sample");

        statistic(() -> benchmark(App::sample), 1024);
    }

    static void statistic(LongSupplier benchmark, int count) {
        var deltaAverage = 0.0;
        var prev = benchmark.getAsLong();
        for (var n = 0; true; ++n) {
            final var result = benchmark.getAsLong();
            final var c = 1.0 * n / (n + 1);
            final var delta = Math.abs(result - prev);
            prev = result;
            deltaAverage = deltaAverage * c + (delta / (n + 1.0));
            if ((n & (n - 1)) == 0) {

                System.out.print(n);
                System.out.print(" ");
                System.out.print((deltaAverage));
                System.out.print(" ");
                System.out.println(result);
            }
            if (deltaAverage < 0.001) {
                break;
            }
        }

        var results = new long[count];
        var sum = 0;
        for (var i = 0; i < count; ++i) {
            final var result = benchmark.getAsLong();
            results[i] = result;
            sum += result;
        }

        System.out.println(1.0 * sum / count);
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

    static <T> long benchmark(Method method) {
        try {
            final var startTime = System.nanoTime();
            method.invoke(null);
            final var endTime = System.nanoTime();
            return endTime - startTime;
        } catch (Exception e) {
            return -1;
        }
    }

    static Set<Entry<BigInteger, Integer>> sample() {
        var rnd = new BigInteger(5, new Random());
        if (rnd.equals(BigInteger.ZERO)) {
            return Collections.emptySet();
        }

        final var hash = new HashMap<BigInteger, Integer>();

        final var lsb = rnd.getLowestSetBit();
        if (lsb > 0) {
            hash.put(BigInteger.TWO, lsb);
            rnd = rnd.shiftRight(lsb);
        }
        var div = BigInteger.valueOf(3);

        while (!rnd.equals(BigInteger.ONE)) {
            var exp = 0;
            while (rnd.remainder(div).equals(BigInteger.ZERO)) {
                rnd = rnd.divide(div);
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
