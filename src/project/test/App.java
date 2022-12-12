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

import project.lib.InletStream;
import project.lib.StreamBuffer;
import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.lib.scaffolding.ArrayPool;
import project.lib.scaffolding.ByteArrayPool;
import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.SegmentBuffer;
import project.lib.scaffolding.collections.SegmentBufferStrategy;
import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.streaming.BlockBufferWriterProxy;
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

public class App {
    public static void main(String[] args) throws Exception {
        final var keyBundle = RSA.generateKey(9);
        final var encripter = new RSAEncrypter(keyBundle.exponent, keyBundle.modulo);
        final var reader = new InputStreamReader(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4, }))
                .transform(new RSAEncrypter(keyBundle.exponent, keyBundle.modulo));
        System.out.println(BinaryDebug.dumpHex(reader.read(100)));
        final var collected = InletStream.from(new byte[] { 0, 1, 2, 3, 4, })
                .transform(RSAPlainChunked.encripter(keyBundle.exponent, keyBundle.modulo)).collect();

        encripter.writer().write(new byte[] { 0, 1, 2, 3, 4, }, 0, ~5);
        System.out.println(BinaryDebug.dumpHex(encripter.read()));
        System.out.println(BinaryDebug.dumpHex(collected));

        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.standard(), tests);
    }

}

class RSAEncrypter extends BlockTransformerBase<byte[]> {
    RSAEncrypter(BigInteger exponent, BigInteger modulo) {
        super(RSA.plainBlockLength(modulo), ByteArrayPool.instance());
        this.codeBlockLength = RSA.codeBlockLength(modulo);
        this.exponent = exponent;
        this.modulo = modulo;
    }

    private final int codeBlockLength;
    private final BigInteger exponent;
    private final BigInteger modulo;

    @Override
    protected void transform(byte[] buffer, int offset, int length) {
        final var codeBlockLength = this.codeBlockLength;
        final var plain = new BigInteger(1, buffer, offset, StreamUtil.lenof(length));
        final var code = plain.modPow(this.exponent, this.modulo);
        final var bin = code.toByteArray();
        final var writer = this.buffer.writer();

        writer.stage(codeBlockLength);
        final var len = Math.min(codeBlockLength, bin.length);
        final var off = bin.length - len;
        System.arraycopy(bin, off, writer.stagedBuffer(), writer.stagedOffset() + codeBlockLength - len, len);
        if (StreamUtil.isLast(length)) {
            writer.finish(~codeBlockLength);
            this.markCompleted();
        } else {
            writer.finish(codeBlockLength);
        }
    }
}
