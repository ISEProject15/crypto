package project.lib;

import java.io.IOException;

//双方向通信用のストリームを表すインターフェイス
public interface DuplexStream extends java.io.Closeable {
    public OutletStream outlet();

    public InletStream inlet();

    @Override
    default void close() throws IOException {
        this.outlet().close();
        this.inlet().close();
    }
}