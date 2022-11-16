package project.lib;

//双方向通信用のストリームを表すインターフェイス
public interface DuplexStream extends java.io.Closeable {
    // データを受信してdestinationに書き込む．
    // 書き込んだバイト数を返す．ただし，最後のブロックを書き込んだ場合は書き込んだバイト数のnotを返す．
    // スレッドセーフである必要がある．
    public int read(byte[] destination);

    // sourceからlengthバイトを送信する．
    // lengthが負のときは最後のブロックであることを表す．
    // スレッドセーフである必要がある．
    public void write(byte[] source, int length);
}