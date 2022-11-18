package project.lib;

import java.io.Closeable;
import java.io.IOException;

//出力用のストリームを表すインターフェイス
public interface OutletStream extends Closeable {
    // sourceからlengthバイトを送信する．
    // lengthが負のときは最後のブロックであることを表す．
    public void write(byte[] source, int length) throws IOException;
}
