package project.lib.scaffolding.debug;

import project.lib.scaffolding.collections.Sequence;

public class BinaryDebug {
    public static String dumpHex(byte[] bin, int offset, int length) {
        final var builder = new StringBuilder(length);
        for (var i = 0; i < length; ++i) {
            final var b = bin[offset + i];
            builder.append(hexL4(b)).append(hexL4(b >>> 4));
        }
        return builder.toString();
    }

    public static String dumpHexHighlight(byte[] bin, int offset, int length) {
        final var builder = new StringBuilder(length);
        for (var i = 0; i < bin.length; ++i) {
            final var b = bin[i];
            if (i == offset) {
                builder.append("\u001B[31m");
            }
            builder.append(hexL4(b)).append(hexL4(b >>> 4));
            if (i == length + offset - 1) {
                builder.append("\u001B[0m");
            }
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

    public static String dumpHex(Sequence<byte[]> sequence) {
        final var builder = new StringBuilder();
        final var iter = sequence.iterator();
        var first = true;
        while (iter.next()) {
            if (first) {
                first = false;
            } else {
                builder.append('+');
            }

            final var dump = dumpHex(iter.currentBuffer(), iter.currentOffset(), iter.currentLength());
            builder.append(dump);
        }
        return builder.toString();
    }
}
