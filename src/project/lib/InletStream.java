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

    // loaded bytes count; if source was ended,
    // bufferd will be inverse of bytes count.
    private int buffered;
    private final byte[] buffer;

    private final InputStream source;

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public int read(byte[] destination) throws IOException {
        var written = this.flushBuffer(destination);
        if (written < 0) { // source was ended 
            return written;
        }
        if(written < destination.length) {// destination space left
            // load source to the rest of destination
            final var restWritten = this.source.read(destination, written, destination.length - written);
            if (restWritten >= 0) {
                written += restWritten;
            }
        }
        final var bufferWritten = this.loadBuffer();
        //NOTE: if restWritten < 0, also bufferWritten < 0  
        if(bufferWritten < 0) {// source was ended; no data was read, so destination is the last segment.
            return ~written;
        }
        return written;
    }

    // load source to buffer. returns written bytes; if source was ended, returns -1.
    private int loadBuffer() throws IOException
    {
        final var buffer = this.buffer;
        final var buffered = this.buffered;
        if(buffered < 0) {
            return buffered;
        }
        final int written = this.source.read(buffer, buffered, buffer.length - buffered);
        if(written < 0) {//no data left in source; nothing was read.
            this.buffered = ~buffered;
            return written;
        }
        this.buffered = buffered + written;
        return written;
    }

    private int flushBuffer(byte[] destination) {
        final var buffered = this.buffered;
        final var normalized = normalize(buffered);
        final var len = Math.min(destination.length, normalized);
        System.arraycopy(this.buffer, 0, destination, 0, len);

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