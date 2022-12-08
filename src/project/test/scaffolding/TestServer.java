package project.test.scaffolding;

import java.util.ArrayList;

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
        if (!isNullOrEmpty(summary.description)) {
            System.out.print(" : ");
            System.out.print(summary.description);
        }
        if (summary.isSuite()) {
            System.out.println();
            for (final var child : summary.children()) {
                printSummaryIndented(child, indent + 1);
            }
        } else {
            System.out.print("; ");
            System.out.println(summary.exception());
        }
    }

    private static void printIndent(int level) {
        while (level > 0) {
            System.out.print("  ");
        }
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
