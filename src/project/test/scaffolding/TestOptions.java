package project.test.scaffolding;

public class TestOptions {

    private TestOptions(boolean dumpStandardOutput) {
        this.dumpStandardOutput = dumpStandardOutput;
    }

    final boolean dumpStandardOutput;

    public boolean dumpStandardOutput() {
        return this.dumpStandardOutput;
    }
}
