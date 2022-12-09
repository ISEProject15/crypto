package project.test;

import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;

public class App {

    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        final var server = TestExecutor.create();
        server.register(tests);
        server.execute();
    }

}
