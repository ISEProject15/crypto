package project.test;

import java.nio.charset.StandardCharsets;

import project.lib.InletStream;
import project.lib.crypto.algorithm.RSAPlain;
import project.test.scaffolding.ReflectionUtil;
import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestServer;

import static project.lib.scaffolding.debug.BinaryDebug.*;

public class App {

    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        final var server = TestServer.create();
        server.register(tests);
        server.execute();

        final var keyBundle = RSAPlain.generateKey(33);
        final var encripter = RSAPlain.encripter(keyBundle.exponent, keyBundle.modulo);
        final var decripter = RSAPlain.decripter(keyBundle.secret, keyBundle.modulo);
        System.out.println(keyBundle.toString());
        System.out.print("plainBlock:");
        System.out.print(RSAPlain.plainBlockLength(keyBundle.modulo));
        System.out.print(";codeBlock:");
        System.out.print(RSAPlain.codeBlockLength(keyBundle.modulo));
        System.out.println();
        final var plainText = "yo text 09328109jdiaondwoiansdwohia79201j20ujdw0a9jd092u1j923j10ijsd89jw09a";
        final var plain = plainText.getBytes(StandardCharsets.UTF_8);
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

        final var decodedText = new String(decoded, 0, len, StandardCharsets.UTF_8);
        System.out.println(decodedText);
        System.out.println(decodedText.equals(plainText));
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
