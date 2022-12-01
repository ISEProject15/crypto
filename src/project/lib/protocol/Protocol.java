package project.lib.protocol;

import java.io.IOException;

import project.lib.DuplexStream;

public interface Protocol {
    public String identity();

    // establish client connection include handshaking
    public DuplexStream establishClient(DuplexStream stream, Ion arg) throws IOException;

    // establish server connection include handshaking
    public DuplexStream establishServer(DuplexStream stream, Ion arg) throws IOException;
}
