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
    public int read() throws IOException {
        final var buffer = this.buffer;
        if (this.bufferedCount() <= 0) {
            final var written = this.loadBuffer();
            if (written < 0) {
                return -1;
            }
        }
        final var result = buffer[this.bufferOffset];
        this.bufferOffset++;
        this.bufferedCount(this.bufferedCount() - 1);
        return result;
    }

    private int bufferedCount() {
        return StreamUtil.lenof(this.buffered);
    }

    private void bufferedCount(int count) {
        this.buffered = count ^ StreamUtil.flagof(this.buffered);
    }

    private boolean sourceEnded() {
        return this.buffered < 0;
    }

    // load source to buffer. returns written bytes;
    // if source was ended, returns -1.
    private int loadBuffer() throws IOException {
        final var buffer = this.buffer;
        final var buffered = this.buffered;
        if (this.sourceEnded()) {
            return -1;
        }
        if (buffered > 0) {
            return 0;
        }
        final int written = this.source.read(buffer);
        this.buffered = written;
        return written;
    }
}

class InputToInletStream implements InletStream {
    InputToInletStream(InputStream source) {
        this.source = source;
        this.buffer = -2;
    }

    private static final byte SOURCE_WAS_ENDED = -1;
    private static final byte NO_BYTE_BUFFERED = -2;

    // buffered byte.
    // if buffer == -1, source was ended.
    // if buffer == -2, no byte buffered.
    private int buffer;
    private final InputStream source;

    @Override
    public void close() throws IOException {
        this.source.close();
    }

    @Override
    public int read(byte[] destination) throws IOException {
        if (this.buffer == SOURCE_WAS_ENDED) {
            return -1;
        }
        if (destination.length <= 0) {
            return 0;
        }
        // read buffer to destination
        final var buffered = this.buffer != NO_BYTE_BUFFERED;
        var totalWritten = 0;
        if (buffered) {
            destination[0] = (byte) this.buffer;
            this.buffer = NO_BYTE_BUFFERED;
            totalWritten += 1;
        }

        // read source to the rest of destination
        final var written = this.source.read(destination, totalWritten, destination.length - totalWritten);
        if (written < 0) { // source was ended
            this.buffer = SOURCE_WAS_ENDED;
            return ~totalWritten;
        }

        totalWritten += written;
        // check if source was ended
        this.buffer = this.source.read();
        if (this.buffer == SOURCE_WAS_ENDED) {
            return ~totalWritten;
        }

        return totalWritten;
    }

    @Override
    public InputStream toInputStream() {
        return this.source;
    }
}