package project.lib.scaffolding.streaming;

public interface BufferWriter<T> {
    public void stage(int minimumLength);

    public boolean tryStage(int minimumLength);

    public T stagedBuffer();

    public int stagedOffset();

    public int stagedLength();

    public void finish(int written);
}
