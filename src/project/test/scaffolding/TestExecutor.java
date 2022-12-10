package project.test.scaffolding;

import java.util.stream.Collectors;

import project.scaffolding.debug.AnsiColor;
import project.scaffolding.debug.IndentedAppendable;

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
                appendable.print(AnsiColor.fgGreen).print(succeededText).println(AnsiColor.reset);
            } else {
                final var exception = summary.exception();
                appendable.print(AnsiColor.fgRed).print(exception.getClass().getName());
                if (options.displayExceptionMessage()) {
                    appendable.print(": ").print(exception.getMessage());
                }
                appendable.println(AnsiColor.reset);

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

    private static void printDump(IndentedAppendable appendable, AnsiColor color, String linePrefix, String dump) {
        if (dump == null || dump.isEmpty()) {
            return;
        }
        if (color != null) {
            appendable.print(color);
        }
        var lineNo = 0;

        final var lines = dump.lines().collect(Collectors.toUnmodifiableList());
        final var digits = (int) Math.ceil(Math.log10(lines.size() + 1));
        final var format = "%s%0" + digits + "d: ";
        for (final var line : lines) {
            appendable.print(String.format(format, linePrefix, lineNo)).println(line);
            lineNo++;
        }
        if (color != null) {
            appendable.print(AnsiColor.reset);
        }
    }

    @FunctionalInterface
    private static interface Action {
        public void invoke();
    }
}
