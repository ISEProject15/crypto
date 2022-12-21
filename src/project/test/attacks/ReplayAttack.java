package project.test.attacks;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import project.lib.crypto.algorithm.RSAPlainChunked;
import project.scaffolding.debug.BinaryDebug;

public class ReplayAttack {
    public static void demo() {
        final var random = new Random();
        final var keybundle = RSAPlainChunked.generateKey(9, random);
        final var preEncrypter = RSAPlainChunked.encrypter(keybundle.exponent, keybundle.modulo);

        final var plainText = "sample text";
        final var plainBytes = plainText.getBytes(StandardCharsets.UTF_8);

        System.out.println("plain: " + plainText);

        preEncrypter.writer().write(plainBytes, 0, ~plainBytes.length);

        final var preEncrypted = preEncrypter.read().toArray();
        System.out.println("pre encrypted: " + BinaryDebug.dumpHex(preEncrypted));

        final var encrypter = RSAPlainChunked.encrypter(keybundle.exponent, keybundle.modulo);
        encrypter.writer().write(plainBytes, 0, ~plainBytes.length);

        final var encrypted = encrypter.read().toArray();
        System.out.println("    encrypted: " + BinaryDebug.dumpHex(encrypted));

        final var decrypter = RSAPlainChunked.decrypter(keybundle.secret, keybundle.modulo);
        decrypter.writer().write(encrypted, 0, ~encrypted.length);
        final var decryptedBytes = decrypter.read().toArray();
        final var len = lastIndexOrLength(decryptedBytes, (byte) 0);

        System.out.println("decrypted bytes: " + BinaryDebug.dumpHex(decryptedBytes));

        final var decrypted = new String(decryptedBytes, 0, len, StandardCharsets.UTF_8);
        System.out.println("decrypted: " + decrypted);
        System.out.println("plain == decrypted: " + (plainText.equals(decrypted)));
    }

    static int lastIndexOrLength(byte[] array, byte val) {
        for (var i = array.length - 1; i >= 0; --i) {
            if (array[i] == val) {
                return i;
            }
        }
        return array.length;
    }
}
