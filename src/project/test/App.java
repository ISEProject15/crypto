package project.test;

import project.test.scaffolding.TestCollector;
import project.test.scaffolding.TestExecutor;
import project.test.scaffolding.TestExecutorOptions;

public class App {

    public static void main(String[] args) throws Exception {
        final var tests = TestCollector.collect("project.test.unitTests");
        TestExecutor.execute(TestExecutorOptions.standard(), tests);
    }

}
