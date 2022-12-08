package project.test.scaffolding;

public abstract class TestAgent {
    protected TestAgent(String domain, String description) {
        this.domain = domain;
        this.description = description;
    }

    public final String domain;

    public final String description;

    public abstract TestSummary execute();
}
