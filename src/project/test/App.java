package project.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.print.attribute.standard.PrinterMessageFromOperator;

import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.BufferWriter;
import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.streaming.StreamUtil;
import project.scaffolding.debug.IndentedAppendable;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;

public class App {
    public static void main(String[] args) throws Exception {
        final var reader = new BufferedReader(new InputStreamReader(System.in));
        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.standard(), tests);
    }

}
