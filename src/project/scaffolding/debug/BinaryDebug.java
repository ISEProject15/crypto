package project.scaffolding.debug;

import java.io.IOException;

import project.lib.scaffolding.collections.Sequence;

public class BinaryDebug {
    private static void dumpHexNoException(Appendable builder, byte[] bin, int offset, int length) {
        try {
            dumpHex(builder, bin, offset, length);
        } catch (Exception e) {
        }
    }

    private static void dumpHexNoException(Appendable builder, byte b) {
        try {
            dumpHex(builder, b);
        } catch (Exception e) {
        }
    }

    public static void dumpHex(Appendable builder, byte b) throws IOException {
        builder.append(hexL4(b)).append(hexL4(b >>> 4));
    }

    public static void dumpHex(Appendable builder, byte[] bin, int offset, int length) throws IOException {
        for (var i = 0; i < length; ++i) {
            final var b = bin[offset + i];
            dumpHex(builder, b);
        }
    }

    public static String dumpHex(byte[] bin, int offset, int length) {
        final var builder = new StringBuilder(length);
        dumpHexNoException(builder, bin, offset, length);
        return builder.toString();
    }

    public static String dumpHexHighlight(byte[] bin, int offset, int length) {
        final var builder = new StringBuilder(length);
        for (var i = 0; i < bin.length; ++i) {
            final var b = bin[i];
            if (i == offset) {
                builder.append(AnsiColor.bgRed);
            }
            dumpHexNoException(builder, b);
            if (i == length + offset - 1) {
                builder.append(AnsiColor.reset);
            }
        }
        return builder.toString();
    }

    public static String dumpHex(byte[] bin) {
        return dumpHex(bin, 0, bin.length);
    }

    public static String dumpHexDiff(byte[] bin0, byte[] bin1) {
        final var minLen = Math.min(bin0.length, bin1.length);
        final var maxLen = Math.max(bin0.length, bin1.length);
        final var builder = new StringBuilder(maxLen * 2);
        builder.append("src:");
        for (var i = 0; i < minLen; ++i) {
            dumpHexNoException(builder, bin0[i]);
        }
        if (minLen < bin0.length) {
            builder.append(AnsiColor.bgGreen);
            for (var i = minLen; i < bin0.length; ++i) {
                dumpHexNoException(builder, bin0[i]);
            }
            builder.append(AnsiColor.reset);
        }
        builder.append(System.lineSeparator());
        builder.append("trg:");
        var inDifferentSection = false;
        for (var i = 0; i < minLen; ++i) {
            final var b0 = bin0[i];
            final var b1 = bin1[i];

            final var differ = b0 != b1;
            if (inDifferentSection && !differ) {// from different section into same section
                builder.append(AnsiColor.reset);
            }
            if (!inDifferentSection && differ) {// from same section into different section
                builder.append(AnsiColor.bgRed);
            }
            dumpHexNoException(builder, b1);
            inDifferentSection = differ;
        }
        if (inDifferentSection) {
            builder.append(AnsiColor.reset);
        }

        if (minLen < bin1.length) {
            builder.append(AnsiColor.bgRed);
            for (var i = minLen; i < bin1.length; ++i) {
                dumpHexNoException(builder, bin1[i]);
            }
            builder.append(AnsiColor.reset);
        }

        return builder.toString();
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
        while (iter.hasNext()) {
            iter.move();
            if (first) {
                first = false;
            } else {
                builder.append('+');
            }

            dumpHexNoException(builder, iter.currentBuffer(), iter.currentOffset(), iter.currentLength());
        }
        return builder.toString();
    }
}
