package project.test.scaffolding.benchmark;

import java.util.ArrayList;

public final class BenchmarkServer {
    private BenchmarkServer() {

    }

    private static Summary current;

    public static void clear() {
        current = null;
    }

    public static void initialize() {
        current = new Summary(null);
    }

    public static BenchmarkSummary terminate() {
        final var summary = current;
        current = null;
        return summary;
    }

    public static BenchmarkSummary summary() {
        return current;
    }

    public static void enter(String name) {
        if (current == null) {
            return;
        }

        final var summary = new Summary(name);
        if (current == null) {
            current = summary;
        } else {
            summary.parent = current;
            current.add(summary);
            current = summary;
        }
    }

    public static void enter() {
        if (current == null) {
            return;
        }

        final var stacktrace = Thread.currentThread().getStackTrace();
        final var caller = stacktrace[stacktrace.length - 1];
        enter(caller.getMethodName());
    }

    public static BenchmarkSummary leave() {
        if (current == null) {
            return null;
        }

        final var summary = current;
        current = summary.parent;
        return summary;
    }

    public static void increment() {
        if (current == null) {
            return;
        }
        current.programCount++;
    }

    private static final class Summary extends BenchmarkSummary {
        protected Summary(String name) {
            this.children = new ArrayList<>();
            this.name = name;
        }

        private final String name;
        private Summary parent = null;
        private long programCount = 0;
        private final ArrayList<BenchmarkSummary> children;

        public Summary parent() {
            return this.parent;
        }

        public void add(Summary summary) {
            summary.parent = this;
            children.add(summary);
        }

        @Override
        public long programCount() {
            return this.programCount;
        }

        @Override
        public Iterable<BenchmarkSummary> childrenSummaries() {
            return this.children;
        }

        @Override
        public String name() {
            return this.name;
        }
    }
}
