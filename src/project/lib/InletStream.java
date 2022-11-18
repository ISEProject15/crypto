package project.lib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

//入力用のストリームを表すインターフェイス
public interface InletStream extends Closeable {
    public static InletStream from(InputStream source) {
        return new InputToInletStream(source);
    }

    // データを受信してdestinationに書き込む．
    // 書き込んだバイト数を返す．ただし，最後のブロックを書き込んだ場合は書き込んだバイト数のnotを返す．
    public int read(byte[] destination) throws IOException;

    public default InputStream toInputStream() {
        return new InletToInputStream(this);
    }
}

class InletToInputStream extends InputStream {
    private static int normalize(int num) {
        return num ^ (num >> 31);
    }
    private static final int DefaultBufferSize = 1024;

    InletToInputStream(InletStream source) {
        this.source = source;
        this.buffer = new byte[DefaultBufferSize];
        this.buffered = 0;
        this.bufferOffset = 0;
    }

    private final InletStream source;
    private final byte[] buffer;
    private int buffered;
    private int bufferOffset;


    @Override 
    public int read() {
        final var buffer = this.buffer;
        if(this.bufferedCount() <= 0) {
            final var written = this.loadBuffer();
            if(written < 0) {
                return -1;
            }
        }
        final var result = buffer[this.bufferOffset];
        this.bufferOffset++;
        this.buffered = this.bufferedCount(this.bufferedCount() - 1);
        return result;
    }

    private int bufferedCount() {
        return normalize(this.buffered);   
    }
    private void bufferedCount(int count) {
        var mask = this.buffered >> 31;
        this.buffered = count ^ mask;   
    }

    private bool sourceEnded() {
        return this.buffered < 0;
    }
    
    // load source to buffer. returns written bytes; if source was ended, returns -1.
    private int loadBuffer() {
        final var buffer = this.buffer;
        final var offset = this.bufferOffset;
        final var buffered = this.buffered;
        if(this.sourceEnded()) {
            return -1;
        } 
        final int written = this.source.read(buffer, buffered, buffer.length - buffered);
        if(written < 0) {//no data left in source; nothing was read.
            this.buffered = ~buffered;
            return written;
        }
        this.buffered = buffered + written;
        return written;
    }
}

class InputToInletStream implements InletStream {
    private static int normalize(int num) {
        return num ^ (num >> 31);
    }

    private static final int DefaultBufferSize = 1024;

    InputToInletStream(InputStream source) {
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
        this.source.close();
    }

    @Override
    public int read(byte[] destination) throws IOException {
        var written = this.flushBuffer(destination);
        if (written < 0) {// source was ended 
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
    @Override
    public InputStream toInputStream() {
        return this.source;
    }

    private bool sourceEnded() {
        return this.buffered < 0;
    }
    
    // load source to buffer. returns written bytes; if source was ended, returns -1.
    private int loadBuffer() {
        final var buffer = this.buffer;
        final var offset = this.bufferOffset;
        final var buffered = this.buffered;
        if(this.sourceEnded()) {
            return -1;
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