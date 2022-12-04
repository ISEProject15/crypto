package project.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import project.lib.InletStream;
import project.lib.TransformedInletStream;
import project.lib.crypto.algorithm.RSAPlain;

import static project.lib.scaffolding.debug.BinaryDebug.*;

public class App {
    public static void main(String[] args) throws Exception {
        final var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            final var keyBundle = RSAPlain.generateKey(9);
            final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
            final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
            System.out.println(keyBundle.toString());

            final var codeInlet = InletStream.from(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (byte) 0xFF })
                    .transform(encripter);
            final var code = codeInlet.collect();

            System.out.println(" code:" + dumpHex(code));
            final var plainInlet = new TransformedInletStream(InletStream.from(code), decripter);
            System.out.println("plain:" + dumpHex(plainInlet.collect()));

            final var line = reader.readLine();
            if (!line.isEmpty()) {
                break;
            }
        }
    }
}
