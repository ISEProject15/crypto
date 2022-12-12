package project.lib.scaffolding.streaming;

@FunctionalInterface
public interface BufferWriterFinishedCallback<T> {
    public void finished(T buffer, int offset, int length);
}
