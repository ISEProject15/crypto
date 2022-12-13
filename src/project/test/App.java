package project.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.streaming.BlockBufferWriterListenerProxy;
import project.lib.scaffolding.streaming.BlockTransformerBase;
import project.lib.scaffolding.streaming.BufferWriter;
import project.lib.scaffolding.streaming.DefaultPooledBufferWriter;
import project.lib.scaffolding.streaming.StreamUtil;
import project.lib.scaffolding.streaming.Transformer;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;
import project.lib.scaffolding.streaming.InputStreamReader;
import project.lib.scaffolding.streaming.StreamBuffer;

public class App {
    public static void main(String[] args) throws Exception {
        final var keyBundle = RSA.generateKey(9);
        final var encripter = new RSAEncrypter(keyBundle.exponent, keyBundle.modulo);
        final var reader = new InputStreamReader(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4, }))
                .transform(new RSAEncrypter(keyBundle.exponent, keyBundle.modulo));
        System.out.println(
                BinaryDebug
                        .dumpHex(reader.transform(new RSADecrypter(keyBundle.secret, keyBundle.modulo)).readTotally()));

        encripter.writer().write(new byte[] { 0, 1, 2, 3, 4, }, 0, ~5);
        System.out.println(BinaryDebug.dumpHex(encripter.read()));

        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.standard(), tests);
    }

}
