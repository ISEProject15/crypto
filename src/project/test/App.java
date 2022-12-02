package project.test;

import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        final var keyBundle = RSAPlain.generateKey(32);
        final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
        final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
        System.out.println(keyBundle.toString());
        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, };
        System.out.println(Arrays.toString(plain));
        final var code = encripter.transform(plain, 0, ~plain.length);
        System.out.println(Arrays.toString(code));
        final var decoded = decripter.transform(code, 0, ~code.length);
        System.out.println(Arrays.toString(decoded));
    }
}

abstract class Transformer {
    // if length < 0, source is last segment and its size is ~length
    public abstract byte[] transform(byte[] source, int offset, int length);
}
