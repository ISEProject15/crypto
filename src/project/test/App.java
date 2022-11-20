package project.test;

import project.lib.InletStream;
import project.lib.StreamBuffer;
import project.lib.StreamUtil;
import project.lib.TransformedInletStream;
import project.lib.Transformer;
import project.lib.protocol.Ion;
import project.lib.protocol.MetaMessage;
import project.lib.protocol.MetaMessageSerializer;

import static project.lib.protocol.IonBuilder.assoc;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        final var parser = project.lib.protocol.MetaMessageSerializer.instance;
        final var msg = parser.deserialize("id@0:1:key001=v001&key002=v002;&key02=v02;&key1=v1\n");
        final var str = jsonify(msg.body);
        System.out.println(str);

        final var created = MetaMessage.of("id", Ion.of(assoc("a", "b").assoc("c", assoc("d", "e"))));
        final var serialized = MetaMessageSerializer.instance.serialize(created);
        System.out.println(serialized);

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
                segment.length(len * 2);
                buffer.push(segment);
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

    private static String jsonify(Ion body) {
        return body.match((atom) -> {
            return "\"" + atom.text() + "\"";
        }, (mapping) -> {
            final var builder = new java.lang.StringBuilder();
            builder.append("{");
            var isFirst = true;
            for (final var entry : mapping.entries()) {
                if (!isFirst) {
                    builder.append(",");
                }
                isFirst = false;
                final var key = entry.getKey();
                final var value = entry.getValue();
                builder.append('"').append(key).append('"').append(":").append(jsonify(value));
            }

            return builder.append("}").toString();
        });
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