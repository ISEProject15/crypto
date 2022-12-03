package project.lib.scaffolding.debug;

public class BinaryDebug {
    public static String dumpHex(byte[] bin, int offset, int length) {
        final var builder = new StringBuilder(length);
        for (final var b : bin) {
            builder.append(hexL4(b)).append(hexL4(b >>> 4));
        }
        return builder.toString();
    }

    public static String dumpHex(byte[] bin) {
        return dumpHex(bin, 0, bin.length);
    }

    public static char hexL4(int num) {
        num = (num & 0xF);
        if (num < 10) {
            return (char) ('0' + num);
        }
        return (char) ('A' - 10 + num);
    }
}
