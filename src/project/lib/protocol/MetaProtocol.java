package project.lib.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import project.lib.DuplexStream;
import project.lib.StreamReader;

// server, client のペアを定義する
public class MetaProtocol {
    private MetaProtocol(ProtocolRegistry registry) {
        this.registry = registry;
    }

    public final ProtocolRegistry registry;

    public DuplexStream establishClient(DuplexStream stream) throws IOException {
        final var inlet = stream.inlet();
        final var outlet = stream.outlet();
        final var array = IonBuilder.array();
        for (final var protocol : this.registry.available()) {
            array.add(protocol.identity());
        }
        final var code = IonCodec.instance.encode(array);
        final var bytes = code.toString().getBytes(StandardCharsets.UTF_8);
        final var reader = StreamReader.create(inlet);

        // send available protocol identity list
        outlet.write(bytes, bytes.length);
        // receive protocol identity and protocol initialization arguments
        ;
        return null;
    }

    public DuplexStream establishServer(DuplexStream stream) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}