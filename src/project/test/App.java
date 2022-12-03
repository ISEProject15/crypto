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
            final var keyBundle = RSAPlain.generateKey(16);
            final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
            final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
            System.out.println(keyBundle.toString());

            final var input = new ByteArrayInputStream(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
            final var inlet = new TransformedInletStream(InletStream.from(input), encripter);
            System.out.println(Arrays.toString(inlet.collect()));
            final var line = reader.readLine();
            if (!line.isEmpty()) {
                break;
            }
        }
    }
}