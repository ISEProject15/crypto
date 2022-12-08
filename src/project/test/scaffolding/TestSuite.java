package project.test.scaffolding;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestSuite extends TestAgent {
    public TestSuite(String domain, String description, Iterable<TestAgent> agents) {
        super(domain, description);
        this.agents = agents;
    }

    private final Iterable<TestAgent> agents;

    @Override
    public TestSummary execute() {
        final var results = StreamSupport.stream(this.agents.spliterator(), false).map(agent -> agent.execute())
                .collect(Collectors.toUnmodifiableList());
        return TestSummary.withChildren(domain, description, results);
    }
}
