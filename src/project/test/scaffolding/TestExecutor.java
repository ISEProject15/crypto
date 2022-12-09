package project.test.scaffolding;

import project.scaffolding.debug.AnsiColor;
import project.scaffolding.debug.IndentedAppendable;

public final class TestExecutor {
    private TestExecutor() {

    }

    // execute tests and print results
    public static void execute(TestAgent... agents) {
        final var builder = new StringBuilder();
        for (final var agent : agents) {
            final var summary = agent.execute();
            printSummary(summary, IndentedAppendable.create(builder, "  "));
        }
        System.out.println(builder);
    }

    private static void printSummary(TestSummary summary, IndentedAppendable appendable) {
        appendable.print(summary.domain);
        appendable.indent();
        if (summary.isSuite()) {
            appendable.println();
            for (final var child : summary.children()) {
                printSummary(child, appendable);
            }
        } else {
            appendable.print("; ");
            if (summary.succeeded()) {
                appendable.print(AnsiColor.fgGreen).print("OK").println(AnsiColor.reset);
            } else {
                appendable.print(AnsiColor.fgRed).print(summary.exception()).println(AnsiColor.reset);
            }
        }
        appendable.deindent();
    }
}
