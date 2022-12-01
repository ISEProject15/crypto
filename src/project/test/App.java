package project.test;

import java.math.BigInteger;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        final var keyBundle = RSAPlain.generateKey(8);
        System.out.println(keyBundle.toString());

        final var test = BigInteger.valueOf(0xCEF);
        final var bin = test.toByteArray();
        final var bin1 = new byte[3];
        System.arraycopy(bin, 0, bin1, bin1.length - bin.length, 2);

        System.out.println(test);
        System.out.println(Arrays.toString(bin));
        System.out.println(Arrays.toString(bin1));
        System.out.println(new BigInteger(bin1));

        final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
        final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);

        final var plain = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, };
        System.out.println(Arrays.toString(plain));
        final var code = encripter.transform(plain, 0, plain.length);
        System.out.println(Arrays.toString(code));
        final var decoded = decripter.transform(code, 0, code.length);
        System.out.println(Arrays.toString(decoded));
    }
}

abstract class Transformer {
    // if length < 0, source is last segment and its size is ~length
    public abstract byte[] transform(byte[] source, int offset, int length);
}
