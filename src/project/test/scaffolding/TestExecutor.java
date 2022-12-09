package project.test.scaffolding;

import java.util.ArrayList;

import project.scaffolding.debug.AnsiColor;
import project.scaffolding.debug.IndentedAppendable;

public class TestExecutor {
    public static TestExecutor create() {
        return new TestExecutor();
    }

    private TestExecutor() {
        this.agents = new ArrayList<>();
    }

    private final ArrayList<TestAgent> agents;

    public void register(TestAgent agent) {
        this.agents.add(agent);
    }

    // execute tests and print results
    public void execute() {
        final var builder = new StringBuilder();
        for (final var agent : this.agents) {
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
