package project.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.net.ssl.StandardConstants;

import project.lib.InletStream;
import project.lib.StreamBuffer;
import project.lib.TransformedInletStream;
import project.lib.crypto.algorithm.RSAPlain;
import project.lib.protocol.IonBuilder;

import static project.lib.scaffolding.debug.BinaryDebug.*;

public class App {

    public static void main(String[] args) throws Exception {
        final var keyBundle = RSAPlain.generateKey(33);
        final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
        final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
        System.out.println(keyBundle.toString());
        System.out.print("plainBlock:");
        System.out.print(RSAPlain.plainBlockLength(keyBundle.modulo));
        System.out.print(";codeBlock:");
        System.out.print(RSAPlain.codeBlockLength(keyBundle.modulo));
        System.out.println();

        final var plain = "yo text 09328109jdiaondwoiansdwohia79201j20ujdw0a9jd092u1j923j10ijsd89jw09a"
                .getBytes(StandardCharsets.UTF_8);
        System.out.println("  plain:" + dumpHex(plain));
        final var codeInlet = InletStream.from(plain)
                .transform(encripter);
        final var code = codeInlet.collect();

        System.out.println("   code:" + dumpHex(code));
        final var plainInlet = InletStream.from(code).transform(decripter);
        final var decoded = plainInlet.collect();
        System.out.println("decoded:" + dumpHex(decoded));
        for (var i = 0; i < Math.min(plain.length, decoded.length); ++i) {
            if (plain[i] != decoded[i]) {
                System.out.println(dumpHexDiff(plain, decoded));

                throw new IllegalStateException();
            }
        }

        final var index = indexOf(decoded, 0, decoded.length, (byte) 0);
        final var len = index < 0 ? decoded.length : index;
        System.out.println(index);

        final var str = new String(decoded, 0, len, StandardCharsets.UTF_8);
        System.out.println(str);
    }

    static int indexOf(byte[] bin, int offset, int length, byte value) {
        for (var i = 0; i < length; ++i) {
            if (bin[i + offset] == value) {
                return i;
            }
        }
        return -1;
    }
}
