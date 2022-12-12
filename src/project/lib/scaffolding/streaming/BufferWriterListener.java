package project.lib.scaffolding.streaming;

@FunctionalInterface
public interface BufferWriterListener<T> {
    public void finished(T buffer, int offset, int length);
}
