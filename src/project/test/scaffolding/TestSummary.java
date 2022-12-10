package project.test.scaffolding;

import java.util.stream.StreamSupport;

public class TestSummary {
    public static TestSummary succeeded(String domain, String stdout, String stderr) {
        return new TestSummary(domain, stdout, stderr, null);
    }

    public static TestSummary withException(String domain, String stdout, String stderr,
            Throwable exception) {
        return new TestSummary(domain, stdout, stderr, exception);
    }

    public static TestSummary withChildren(String domain, Iterable<TestSummary> children) {

        return new TestSummary(domain, null, null, children);
    }

    private TestSummary(String domain, String stdout, String stderr, Object obj) {
        this.domain = domain;
        this.childrenOrException = obj;
        this.standardOutputDump = stdout;
        this.standardErrorDump = stderr;
    }

    public final String domain;
    private final Object childrenOrException;
    private final String standardOutputDump;
    private final String standardErrorDump;

    public String standardOutputDump() {
        return this.standardOutputDump;
    }

    public String standardErrorDump() {
        return this.standardErrorDump;
    }

    public boolean succeeded() {
        final var children = this.children();
        if (children != null) {
            return StreamSupport.stream(children.spliterator(), false).allMatch(c -> c.succeeded());
        }
        final var exception = this.exception();
        return exception == null;
    }

    @SuppressWarnings("unchecked")
    public Iterable<TestSummary> children() {
        if (this.childrenOrException instanceof Iterable<?> children) {
            return (Iterable<TestSummary>) children;
        }
        return null;
    }

    public Throwable exception() {
        if (this.childrenOrException instanceof Throwable exception) {
            return exception;
        }
        return null;
    }

    public boolean isSuite() {
        return this.childrenOrException instanceof Iterable<?>;
    }
}
