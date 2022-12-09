package project.test.scaffolding;

import java.util.ArrayList;

import project.scaffolding.debug.AnsiColor;

public class TestServer {
    public static TestServer create() {
        return new TestServer();
    }

    private TestServer() {
        this.agents = new ArrayList<>();
    }

    final ArrayList<TestAgent> agents;

    public void register(TestAgent agent) {
        this.agents.add(agent);
    }

    // execute tests and print results
    public void execute() {
        for (final var agent : this.agents) {
            final var summary = agent.execute();
            printSummary(summary);
        }
    }

    private static void printSummary(TestSummary summary) {
        printSummaryIndented(summary, 0);
    }

    private static void printSummaryIndented(TestSummary summary, int indent) {
        printIndent(indent);
        System.out.print(summary.domain);
        if (summary.isSuite()) {
            System.out.println();
            for (final var child : summary.children()) {
                printSummaryIndented(child, indent + 1);
            }
        } else {
            System.out.print("; ");
            if (summary.succeeded()) {
                System.out.print(AnsiColor.fgGreen);
                System.out.print("✓");
                System.out.println(AnsiColor.reset);
            } else {
                System.out.print(AnsiColor.fgRed);
                System.out.print(summary.exception());
                System.out.println(AnsiColor.reset);
            }
        }
    }

    private static void printIndent(int level) {
        while (level > 0) {
            System.out.print("  ");
            level--;
        }
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
