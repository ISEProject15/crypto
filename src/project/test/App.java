package project.test;

import project.lib.InletStream;
import project.lib.TransformedInletStream;
import project.lib.Transformer;
import project.lib.protocol.MetaMessageBuilder;
import project.lib.protocol.MetaMessage.Body;
import static project.lib.protocol.MetaMessageBuilder.assoc;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        final var parser = project.lib.protocol.MetaMsgParser.instance;
        final var msg = parser.parse("id@0:1:key001=v001&key002=v002;&key02=v02;&key1=v1");
        final var str = jsonify(msg.body());
        System.out.println(str);

        final var created = MetaMessageBuilder.create("id", assoc("a", "b").assoc("c", assoc("d", "e")));
        System.out.println(created.identity() + "@" + jsonify(created.body()));

        final var byteStream = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4, 5 });
        final var inletStream = InletStream.from(byteStream);
        final var transformed = new TransformedInletStream(inletStream, new DuplicationTransformer());
        final var buffer = new byte[3];
        while (true) {
            final var written = transformed.read(buffer);
            System.out.println("written:" + written + "=" + Arrays.toString(buffer));
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
        private byte[] buffer;
        private int buffered;

        private int bufferedCount() {
            return this.buffered ^ (this.buffered >> 31);
        }

        @Override
        public int transform(byte[] source, int sourceLength, byte[] destination) {
            if (this.buffer != null) {
                final var len = Math.min(this.bufferedCount(), destination.length);
                System.arraycopy(this.buffer, 0, destination, 0, len);
                final var left = this.bufferedCount() - len;
                
            }

            return 0;
        }

    }

    private static String jsonify(Body body) {
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