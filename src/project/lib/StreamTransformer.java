package project.lib;

public interface StreamTransformer {
    public DuplexStream transform(DuplexStream stream);
}
