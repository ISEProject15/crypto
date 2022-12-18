package project.test.scaffolding.benchmark;

public abstract class BenchmarkSummary {

    public boolean isGlobal() {
        return this.name() == null;
    }

    public abstract BenchmarkSummary parent();

    public abstract String name();

    public abstract long programCount();

    public abstract Iterable<BenchmarkSummary> childrenSummaries();

    public long totalProgramCount() {
        var count = this.programCount();
        for (final var s : this.childrenSummaries()) {
            count += s.totalProgramCount() + 1;
        }
        return count;
    }
}
