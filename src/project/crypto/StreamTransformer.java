package project.crypto;

import project.DuplexStream;

public interface StreamTransformer {
    public DuplexStream transform(DuplexStream stream);
}
