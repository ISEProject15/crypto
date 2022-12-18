package project.test.scaffolding.drawing;

import project.scaffolding.debug.IndentedAppendable;

public interface GraphEncoder {
    public void encode(Graph graph, IndentedAppendable builder);
}
