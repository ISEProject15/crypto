package project.lib.protocol;

public interface ProtocolRegistry {
    public Protocol initialize(String identity, Ion arg);

    public Iterable<Protocol> available();
}
