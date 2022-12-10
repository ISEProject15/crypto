package project.test.scaffolding;

import project.scaffolding.debug.AnsiColor;

public class TestExecutorOptions {
    private static final byte DISPLAY_NEVER = 0;
    private static final byte DISPLAY_ALWAYS = 1;
    private static final byte DISPLAY_ONFAILED = 2;

    public static TestExecutorOptions quiet() {
        return new TestExecutorOptions(DISPLAY_NEVER, DISPLAY_NEVER, false, false, null, null);
    }

    public static TestExecutorOptions standard() {
        return new TestExecutorOptions(DISPLAY_ONFAILED, DISPLAY_ONFAILED, true, false, null, null);
    }

    public static TestExecutorOptions verbose() {
        return new TestExecutorOptions(DISPLAY_ALWAYS, DISPLAY_ALWAYS, true, true, null, null);
    }

    private TestExecutorOptions(byte stdoutDisplayFlags, byte stderrDisplayFlags, boolean displayExceptionMessage,
            boolean displayExceptionStacktrace,
            AnsiColor stdoutColor,
            AnsiColor stderrColor) {
        this.displayExceptionMessage = displayExceptionMessage;
        this.displayExceptionStacktrace = displayExceptionStacktrace;
        this.standardOutputDisplayFlags = stdoutDisplayFlags;
        this.standardErrorDisplayFlags = stderrDisplayFlags;
        this.standardOutputColor = stdoutColor;
        this.standardErrorColor = stderrColor;

    }

    private final byte standardOutputDisplayFlags, standardErrorDisplayFlags;
    private final boolean displayExceptionMessage, displayExceptionStacktrace;
    private final AnsiColor standardOutputColor, standardErrorColor;

    public AnsiColor standardOutputColor() {
        return this.standardOutputColor;
    }

    public AnsiColor standardErrorColor() {
        return this.standardErrorColor;
    }

    public boolean displayExceptionMessage() {
        return this.displayExceptionMessage;
    }

    public boolean displayExceptionStacktrace() {
        return this.displayExceptionStacktrace;
    }

    public boolean displayStandardOutputAlways() {
        return this.standardOutputDisplayFlags == DISPLAY_ALWAYS;
    }

    public boolean displayStandardErrorAlways() {
        return this.standardErrorDisplayFlags == DISPLAY_ALWAYS;
    }

    public boolean displayStandardOutputOnFailed() {
        return this.standardOutputDisplayFlags == DISPLAY_ONFAILED;
    }

    public boolean displayStandardErrorOnFailed() {
        return this.standardErrorDisplayFlags == DISPLAY_ONFAILED;
    }
}
