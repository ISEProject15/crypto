package project.scaffolding.debug;

import java.util.function.BooleanSupplier;

public class AnsiColor {
    private static final String PREFIX = "\u001B[";

    private static final byte BG = 1;
    private static final byte FG = 2;
    private static final byte MASK = 3;

    public static final AnsiColor reset = new AnsiColor("0m", BG | FG);

    public static final AnsiColor bgBlack = new AnsiColor("40", BG);
    public static final AnsiColor bgRed = new AnsiColor("41", BG);
    public static final AnsiColor bgGreen = new AnsiColor("42", BG);
    public static final AnsiColor bgBlue = new AnsiColor("44", BG);

    private AnsiColor(String color, byte flags) {
        this.color = color;
        this.flags = flags;
    }

    private final String color;
    private final byte flags;

    public AnsiColor combine(AnsiColor other) {
        if (this.isCombined() || other.isCombined()) {
            throw new IllegalStateException();
        }
        return new AnsiColor(this.color + ";" + other.color + "m");
    }

    public boolean isCombined() {
        return this.color.endsWith("m");
    }

    @Override
    public String toString() {
        if (this.isCombined()) {
            return PREFIX + color;
        }
        return PREFIX + color + "m";
    }
}
