package project.test.scaffolding.testing;

public abstract class TestAgent {
    protected TestAgent(String domain) {
        this.domain = domain;
    }

    public final String domain;

    public abstract TestSummary execute();
}
