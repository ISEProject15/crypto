package project.lib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

//入力用のストリームを表すインターフェイス
public interface InletStream extends Closeable {
    public static InletStream from(InputStream source) {
        return new InputToInletStream(source);
    }

    public static InletStream from(byte[] source, int offset, int length) {
        final var arr = new byte[length];
        System.arraycopy(source, offset, arr, 0, length);
        return new ByteArrayInletStream(arr, 0, arr.length);
    }

    public static InletStream from(byte[] source, int offset) {
        return from(source, offset, source.length);
    }

    public static InletStream from(byte[] source) {
        return from(source, 0);
    }

    // データを受信してdestinationに書き込む．
    // 書き込んだバイト数を返す．ただし，最後のブロックを書き込んだ場合は書き込んだバイト数のnotを返す．
    public int read(byte[] destination, int offset, int length) throws IOException;

    public default int read(byte[] destination, int offset) throws IOException {
        return this.read(destination, offset, destination.length);
    }

    public default int read(byte[] destination) throws IOException {
        return this.read(destination, 0);
    }

    // 次のreadの読み取りで推奨されるdestinationのサイズを返す．
    // -1が返されたとき，推奨されるサイズは未定であることを示す．
    public default int preferredBufferSize() {
        return -1;
    }

    public default byte[] collect() throws IOException {
        final var buffer = new StreamBuffer();

        while (true) {
            final var segment = buffer.stage(this.preferredBufferSize());
            final var written = this.read(segment.buffer, segment.offset(), segment.length());
            buffer.notifyWritten(StreamUtil.lenof(written));
            if (StreamUtil.isLast(written)) {
                break;
            }
        }
        final var collected = buffer.toArray();
        buffer.close();
        return collected;
    }

    public default InputStream toInputStream() {
        return new InletToInputStream(this);
    }
}

class ByteArrayInletStream implements InletStream {
    ByteArrayInletStream(byte[] source, int offset, int length) {
        this.source = source;
        this.bound = offset + StreamUtil.lenof(length);
        this.offset = offset;
    }

    private int offset;
    private final int bound;
    private final byte[] source;

    @Override
    public void close() throws IOException {

    }

    @Override
    public int read(byte[] destination, int offset, int length) throws IOException {
        final var rest = this.bound - this.offset;
        final var len = Math.min(rest, length);
        System.arraycopy(source, this.offset, destination, offset, len);
        this.offset += len;
        if (rest <= len) {
            return ~len;
        }
        return len;
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

    // load source to buffer. returns written bytes;
    // if source was ended, returns -1.
    private int loadBuffer() throws IOException {
        final var buffer = this.buffer;
        final var buffered = this.buffered;
        if (StreamUtil.isLast(buffered)) {
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
    public int read(byte[] destination, int offset, int length) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException();
        }
        if (offset + length > destination.length) {
            throw new IllegalArgumentException();
        }
        if (this.buffer == SOURCE_WAS_ENDED) {
            return -1;
        }
        if (length <= 0) {
            return 0;
        }
        // read buffer to destination
        final var buffered = this.buffer != NO_BYTE_BUFFERED;
        var totalWritten = 0;
        if (buffered) {
            destination[offset] = (byte) this.buffer;
            this.buffer = NO_BYTE_BUFFERED;
            totalWritten += 1;
        }

        // read source to the rest of destination
        final var written = this.source.read(destination, totalWritten + offset, destination.length - totalWritten);
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
    public int preferredBufferSize() {
        try {
            return this.source.available();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public InputStream toInputStream() {
        return this.source;
    }
}