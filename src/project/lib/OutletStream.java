package project.lib;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

//出力用のストリームを表すインターフェイス
public interface OutletStream extends Closeable {
    public static OutletStream from(OutputStream stream) {
        return new OutputToOutletStream(stream);
    }

    // sourceからlengthバイトを送信する．
    // lengthが負のときは最後のブロックであることを表す．
    public void write(byte[] source, int length) throws IOException;

    public void flush() throws IOException;
}

class OutputToOutletStream implements OutletStream {
    OutputToOutletStream(OutputStream source) {
        this.source = source;
    }

    private final OutputStream source;
    @Override
    public void write(byte[] source, int length) throws IOException {
        this.source.write(source, 0, length);
    }

    @Override
    public void flush() {
        this.source.flush();
    }

    @Override
    public void close() {
        this.source.close();
    } 
}