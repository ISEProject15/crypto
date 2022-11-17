package project.lib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

//入力用のストリームを表すインターフェイス
public interface InletStream extends Closeable {
    public static InletStream from(InputStream source) {
        return new InputConvertedStream(source);
    }

    // データを受信してdestinationに書き込む．
    // 書き込んだバイト数を返す．ただし，最後のブロックを書き込んだ場合は書き込んだバイト数のnotを返す．
    // スレッドセーフである必要がある．
    public int read(byte[] destination) throws IOException;
}

class InputConvertedStream implements InletStream {
    private static int normalize(int num) {
        return num ^ (num >> 31);
    }

    private static final int DefaultBufferSize = 1024;

    InputConvertedStream(InputStream source) {
        this.source = source;
        this.buffer = new byte[DefaultBufferSize];
    }

    private int buffered;// loaded bytes count; if source was ended, bufferd will be inverse of bytes
                         // count.
    private final byte[] buffer;

    private final InputStream source;

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public int read(byte[] destination) throws IOException {
        var written = this.flushBuffer(destination);
        if (written < 0 || written > 0) {
            return written;
        }
        // TODO: impl load source to the rest of destionation
        this.source.read(destination, written, 0);
        // TODO: impl load source to buffer
        // TODO: impl check source last check
        return 0;
    }

    private int flushBuffer(byte[] destination) {
        var buffered = this.buffered;
        final var normalized = normalize(buffered);
        final var len = Math.min(destination.length, normalized);
        System.arraycopy(this.buffered, 0, destination, 0, len);

        if (buffered < 0) {
            this.buffered = ~(normalized - len);
        } else {
            this.buffered -= len;
        }

        if (buffered < 0 && normalized <= destination.length) {// source was ended and buffer totally flushed
            return ~len;
        }

        return len;
    }
}