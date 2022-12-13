package project.test;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Random;

import project.lib.crypto.algorithm.RSA;
import project.lib.crypto.algorithm.RSAPlain;
import project.lib.crypto.algorithm.RSAPlainChunked;
import project.scaffolding.debug.BinaryDebug;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;
import project.lib.scaffolding.streaming.InputStreamReader;

public class App {
    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.standard(), tests);

        final var keyBundle = RSA.generateKey(17);
        final var encrypter = RSAPlain.encrypter(keyBundle.exponent, keyBundle.modulo);
        final var decrypter = RSAPlain.decrypter(keyBundle.secret, keyBundle.modulo);
        encrypter.writer().write(new byte[] { 0, 1, 2, 3, 4, 5 }, 0, ~6);
        final var encrypted = encrypter.read();
        System.out.println(BinaryDebug.dumpHex(encrypted));
        decrypter.writer().write(encrypted, true);
        final var decrypted = decrypter.read();
        System.out.println(BinaryDebug.dumpHex(decrypted));
    }

}
