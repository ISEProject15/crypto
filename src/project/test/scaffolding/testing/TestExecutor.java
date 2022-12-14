package project.test.scaffolding.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import project.scaffolding.IndentedAppendable;
import project.scaffolding.debug.AnsiColor;

public final class TestExecutor {
    private static final String succeededText = "✓";
    private static final String failedText = "✗";
    private static final String stdoutPrefix = "STDOUT";
    private static final String stderrPrefix = "STDERR";

    private TestExecutor() {

    }

    // execute tests and print results
    public static void execute(TestExecutorOptions options, TestAgent... agents) {
        final var builder = new StringBuilder();
        for (final var agent : agents) {
            final var summary = agent.execute();
            printSummary(options, summary, IndentedAppendable.create(builder, "  "));
        }
        System.out.println(builder);
    }

    private static void printSummary(TestExecutorOptions options, TestSummary summary, IndentedAppendable appendable) {
        appendable.print(summary.domain).print(": ");
        appendable.indent();
        if (summary.isSuite()) {
            if (summary.succeeded()) {
                appendable.print(AnsiColor.fgGreen).print(succeededText);
            } else {
                appendable.print(AnsiColor.fgRed).print(failedText);
            }
            appendable.println(AnsiColor.reset);

            for (final var child : summary.children()) {
                printSummary(options, child, appendable);
            }
        } else {
            if (summary.succeeded()) {
                final var info = summary.information();
                if (info == null) {
                    appendable.print(AnsiColor.fgGreen).print(succeededText).println(AnsiColor.reset);
                } else {
                    appendable.println(info);
                }
            } else {
                final var exception = summary.exception();
                appendable.print(AnsiColor.fgRed).print(exception.getClass().getName());
                if (options.displayExceptionMessage()) {
                    final var msg = exception.getMessage();
                    appendable.print(": ").print(msg == null ? "no further information" : msg);
                }
                appendable.println(AnsiColor.reset);

                if (options.displayExceptionStacktrace()) {
                    printStacktrace(appendable, exception);
                }

                if (options.displayStandardOutputOnFailed()) {
                    printDump(appendable, options.standardOutputColor(), stdoutPrefix, summary.standardOutputDump());
                }

                if (options.displayStandardErrorOnFailed()) {
                    printDump(appendable, options.standardErrorColor(), stderrPrefix, summary.standardErrorDump());
                }
            }
            if (options.displayStandardOutputAlways()) {
                printDump(appendable, options.standardOutputColor(), stdoutPrefix, summary.standardOutputDump());
            }

            if (options.displayStandardErrorAlways()) {
                printDump(appendable, options.standardErrorColor(), stderrPrefix, summary.standardErrorDump());
            }
        }
        appendable.deindent();
    }

    private static void printStacktrace(IndentedAppendable appendable, Throwable e) {
        appendable.print("stacktrace: ");
        final var output = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(output, true));
        appendable.println(output.toString());
    }

    private static void printDump(IndentedAppendable appendable, AnsiColor color, String linePrefix, String dump) {
        if (dump == null || dump.isEmpty()) {
            return;
        }

        var lineNo = 0;

        final var lines = dump.lines().collect(Collectors.toUnmodifiableList());
        final var digits = (int) Math.ceil(Math.log10(lines.size() + 1));
        final var formatBody = "%s%0" + digits + "d:";
        final var format = color == null ? formatBody : (color + formatBody + AnsiColor.reset);
        for (final var line : lines) {
            appendable.print(String.format(format, linePrefix, lineNo)).print(' ').println(line);
            lineNo++;
        }
    }

    @FunctionalInterface
    private static interface Action {
        public void invoke();
    }
}
