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

import project.lib.scaffolding.collections.ArrayUtil;
import project.lib.scaffolding.collections.Sequence;
import project.lib.scaffolding.streaming.BufferWriter;
import project.lib.scaffolding.streaming.StreamUtil;
import project.lib.scaffolding.streaming.Transformer;
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

    /*
     * class RSAEncrypter implements Transformer {
     * 
     * private final Writer writer;
     * private boolean completed;
     * 
     * 
     * @Override
     * public BufferWriter<byte[]> writer() {
     * return this.writer;
     * }
     * 
     * @Override
     * public Sequence<byte[]> read() {
     * // TODO Auto-generated method stub
     * return null;
     * }
     * 
     * @Override
     * public void advance(int consumed) {
     * // TODO Auto-generated method stub
     * 
     * }
     * 
     * @Override
     * public boolean completed() {
     * return this.completed;
     * }
     * 
     * private class Writer implements BufferWriter<byte[]> {
     * 
     * @Override
     * public void stage(int minimumLength) {
     * // TODO Auto-generated method stub
     * 
     * }
     * 
     * @Override
     * public boolean tryStage(int minimumLength) {
     * // TODO Auto-generated method stub
     * return false;
     * }
     * 
     * @Override
     * public byte[] stagedBuffer() {
     * // TODO Auto-generated method stub
     * return null;
     * }
     * 
     * @Override
     * public int stagedOffset() {
     * // TODO Auto-generated method stub
     * return 0;
     * }
     * 
     * @Override
     * public int stagedLength() {
     * // TODO Auto-generated method stub
     * return 0;
     * }
     * 
     * @Override
     * public void finish(int written) {
     * // TODO Auto-generated method stub
     * 
     * }
     * 
     * }
     * }
     */
}
