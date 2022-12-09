package project.scaffolding.debug;

public class AnsiColor {
    static final String SET_COLOR = "\u001B[";
    static final String RESET = "0m";
    static final String COLOR_RESET = SET_COLOR + RESET;
    static final String BG_RED = "41m";
    static final String BG_GREEN = "42m";
    static final String BG_BLUE = "44m";
}
