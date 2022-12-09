package project.test.scaffolding;

import java.util.stream.StreamSupport;

public class TestSummary {
    public static TestSummary succeeded(String domain, CharSequence stdout, CharSequence stderr) {
        return new TestSummary(domain, stdout, stderr, null);
    }

    public static TestSummary withException(String domain, CharSequence stdout, CharSequence stderr,
            Throwable exception) {
        return new TestSummary(domain, stdout, stderr, exception);
    }

    public static TestSummary withChildren(String domain, Iterable<TestSummary> children) {
        CharSequence stdout = null, stderr = null;
        for (final var child : children) {
            if (stdout == null) {
                stdout = child.standardOutputDump;
            } else {
                stdout = JoinedCharSequence.join(stdout, child.standardOutputDump);
            }

            if (stderr == null) {
                stderr = child.standardOutputDump;
            } else {
                stderr = JoinedCharSequence.join(stderr, child.standardErrorDump);
            }
        }

        return new TestSummary(domain, stdout, stderr, children);
    }

    private TestSummary(String domain, CharSequence stdout, CharSequence stderr, Object obj) {
        this.domain = domain;
        this.childrenOrException = obj;
        this.standardOutputDump = stdout;
        this.standardErrorDump = stderr;
    }

    public final String domain;
    private final Object childrenOrException;
    private final CharSequence standardOutputDump;
    private final CharSequence standardErrorDump;

    public CharSequence standardOutputDump() {
        return this.standardOutputDump;
    }

    public CharSequence standardErrorDump() {
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
