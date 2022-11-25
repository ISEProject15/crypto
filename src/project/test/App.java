package project.test;

import project.lib.InletStream;
import project.lib.StreamBuffer;
import project.lib.StreamUtil;
import project.lib.TransformedInletStream;
import project.lib.Transformer;
import project.lib.protocol.IonSerializer;

import static project.lib.protocol.IonBuilder.*;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        final var created = array().add("v0")
                .add(mapping().map("k0", mapping().map("k00", "v00").map("k01", "v01")).map("k1",
                        array().add("").add(array().add("v10").add("v11")).add("v12")).map("k2", "v2"))
                .add("v1");
        final var serialized = IonSerializer.instance.serialize(created);
        System.out.println(serialized);
        final var deserialized = IonSerializer.instance.deserialize(serialized);
        System.out.println(IonSerializer.instance.serialize(deserialized));

        final var byteStream = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4, 5 });
        final var inletStream = InletStream.from(byteStream);
        final var transformed = new TransformedInletStream(inletStream, new DuplicationTransformer());
        final var buffer = new byte[5];
        System.out.println(transformed.preferredBufferSize());
        while (true) {
            final var written = transformed.read(buffer);
            System.out.println("written:" + StreamUtil.lenof(written) + "=" + Arrays.toString(buffer));
            if (written < 0) {
                break;
            }
        }
        transformed.close();

        final var senderloop = new SenderLoop();
        final var receiverLoop = new ReceiverLoop();
        final var wiretapperLoop = new WiretapperLoop();

        senderloop.start();
        receiverLoop.start();
        wiretapperLoop.start();
    }

    private static class DuplicationTransformer implements Transformer {
        final StreamBuffer buffer = new StreamBuffer();

        boolean ended = false;

        @Override
        public int transform(byte[] source, int sourceLength, byte[] destination) {
            if (!this.ended) {
                this.ended = StreamUtil.isLast(sourceLength);
                // read to buffer
                final var len = StreamUtil.lenof(sourceLength);
                final var segment = buffer.rent(len * 2);
                final var buf = segment.buffer;
                for (var i = 0; i < len; ++i) {
                    buf[2 * i] = buf[2 * i + 1] = source[i];
                }
                buffer.push(segment, len * 2);
            }
            final var written = buffer.read(destination);
            if (!StreamUtil.isLast(written)) {// buffer is not empty
                return written;
            }

            if (this.ended) {
                return written;
            } else {
                return StreamUtil.lenof(written);
            }
        }

        @Override
        public int estimatedOutputSize(int sourceLength) {
            return this.buffer.length() + StreamUtil.lenof(sourceLength) * 2;
        }
    }
}

class SenderLoop extends Thread {
    @Override
    public void run() {
        // wait receiver hello
        // send sender hello
        // wait cipher protocol suggestion
        // send cipher protocol prelude
        // receive data
    }
}

class ReceiverLoop extends Thread {
    @Override
    public void run() {
        // Send receiver hello(contains cipher spec)
        // wait sender hello
        // send cipher protocol suggestion
        // wait cipher protocol prelude
        // send data
    }
}

class WiretapperLoop extends Thread {
    @Override
    public void run() {

    }
}