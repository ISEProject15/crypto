package project.test.scaffolding;

import java.util.stream.StreamSupport;

public class TestSummary {
    public static TestSummary succeeded(String domain, String description) {
        return new TestSummary(domain, description, null);
    }

    public static TestSummary withException(String domain, String description, Throwable exception) {
        return new TestSummary(domain, description, exception);
    }

    public static TestSummary withChildren(String domain, String description, Iterable<TestSummary> children) {
        return new TestSummary(domain, description, children);
    }

    private TestSummary(String domain, String description, Object obj) {
        this.domain = domain;
        this.description = description;
        this.childrenOrException = obj;
    }

    public final String domain;
    public final String description;
    private final Object childrenOrException;

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
