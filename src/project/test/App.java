package project.test;

import java.io.ByteArrayInputStream;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;
import project.lib.scaffolding.streaming.InputStreamReader;

public class App {
    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.verbose(), tests);
    }

}
