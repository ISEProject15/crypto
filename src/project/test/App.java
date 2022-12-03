package project.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import project.lib.InletStream;
import project.lib.TransformedInletStream;

public class App {
    public static void main(String[] args) throws Exception {
        final var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            final var keyBundle = RSAPlain.generateKey(32);
            final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
            final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
            System.out.println(keyBundle.toString());

            final var codeInlet = new TransformedInletStream(
                    InletStream.from(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }), encripter);
            final var code = codeInlet.collect();
            System.out.println(Arrays.toString(code));
            final var plainInlet = new TransformedInletStream(InletStream.from(code), decripter);
            System.out.println(Arrays.toString(plainInlet.collect()));

            final var line = reader.readLine();
            if (!line.isEmpty()) {
                break;
            }
        }
    }
}