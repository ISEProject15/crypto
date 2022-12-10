package project.test.scaffolding;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestSuite extends TestAgent {
    public TestSuite(String domain, Iterable<TestAgent> agents) {
        super(domain);
        this.agents = agents;
    }

    public final Iterable<TestAgent> agents;

    @Override
    public TestSummary execute() {
        final var results = StreamSupport.stream(this.agents.spliterator(), false).map(agent -> agent.execute())
                .collect(Collectors.toUnmodifiableList());
        return TestSummary.withChildren(domain, results);
    }
}
