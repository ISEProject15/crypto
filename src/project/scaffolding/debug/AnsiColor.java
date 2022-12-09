package project.scaffolding.debug;

public class AnsiColor {
    private static final String PREFIX = "\u001B[";

    private static final byte FG = 1;
    private static final byte BG = 2;
    private static final byte BGFG = 3;

    public static final AnsiColor reset = new AnsiColor("0m", BGFG);

    public static final AnsiColor bgBlack = new AnsiColor("40", BG);
    public static final AnsiColor bgRed = new AnsiColor("41", BG);
    public static final AnsiColor bgGreen = new AnsiColor("42", BG);
    public static final AnsiColor bgYellow = new AnsiColor("43", BG);
    public static final AnsiColor bgBlue = new AnsiColor("44", BG);
    public static final AnsiColor bgMagnta = new AnsiColor("45", BG);
    public static final AnsiColor bgCyan = new AnsiColor("46", BG);
    public static final AnsiColor bgWhite = new AnsiColor("47", BG);

    public static final AnsiColor bgGray = new AnsiColor("100", BG);
    public static final AnsiColor bgBrightRed = new AnsiColor("101", BG);
    public static final AnsiColor bgBrightGreen = new AnsiColor("102", BG);
    public static final AnsiColor bgBrightYellow = new AnsiColor("103", BG);
    public static final AnsiColor bgBrightBlue = new AnsiColor("104", BG);
    public static final AnsiColor bgBrightMagnta = new AnsiColor("105", BG);
    public static final AnsiColor bgBrightCyan = new AnsiColor("106", BG);
    public static final AnsiColor bgBrightWhite = new AnsiColor("107", BG);

    public static final AnsiColor fgBlack = new AnsiColor("30", FG);
    public static final AnsiColor fgRed = new AnsiColor("31", FG);
    public static final AnsiColor fgGreen = new AnsiColor("32", FG);
    public static final AnsiColor fgYellow = new AnsiColor("33", FG);
    public static final AnsiColor fgBlue = new AnsiColor("34", FG);
    public static final AnsiColor fgMagentaa = new AnsiColor("35", FG);
    public static final AnsiColor fgCyan = new AnsiColor("36", FG);
    public static final AnsiColor fgWhite = new AnsiColor("37", FG);

    public static final AnsiColor fgGray = new AnsiColor("90", FG);
    public static final AnsiColor fgBrightRed = new AnsiColor("91", FG);
    public static final AnsiColor fgBrightGreen = new AnsiColor("92", FG);
    public static final AnsiColor fgBrightYellow = new AnsiColor("93", FG);
    public static final AnsiColor fgBrightBlue = new AnsiColor("94", FG);
    public static final AnsiColor fgBrightMagentaa = new AnsiColor("95", FG);
    public static final AnsiColor fgBrightCyan = new AnsiColor("96", FG);
    public static final AnsiColor fgBrightWhite = new AnsiColor("97", FG);

    private AnsiColor(String color, byte flags) {
        this.color = color;
        this.flags = flags;
    }

    private final String color;
    private final byte flags;

    public AnsiColor combine(AnsiColor other) {
        if ((this.flags & other.flags) != 0) {
            throw new IllegalStateException();
        }

        String fg, bg;
        if (this.flags < other.flags) {
            fg = this.color;
            bg = other.color;
        } else {
            fg = other.color;
            bg = this.color;
        }

        return new AnsiColor(fg + ";" + bg, BGFG);
    }

    public boolean isBg() {
        return (this.flags & BG) != 0;
    }

    public boolean isFg() {
        return (this.flags & FG) != 0;
    }

    public boolean isCombined() {
        return this.flags == 3;
    }

    @Override
    public String toString() {
        return PREFIX + color + "m";
    }
}
