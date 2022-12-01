package project.lib;

// バイトストリームを変換するインターフェース．ステートを持つ． 
public interface Transformer {
    // sourceに渡されたブロックの変換を行い，destinationに書き込む．
    // sourceLengthはsource内のバイト数かそのnotが入っており，sourceLengthが負のときは入力の最後の末尾であることを示す．
    // 戻り値はdestinationに書き込んだバイト数かそのnotであり，戻り値が負のときは変換が終了したことを示す．
    public int transform(byte[] source, int sourceLength, byte[] destination, int destinationOffset,
            int destinationLength);

    // 次のtransformの呼び出しでsourceLengthの入力が与えられた時に出力されるであろうバイト数を返す．
    // -1が返されたとき，出力されるサイズは未定であることを示す．
    public default int estimatedOutputSize(int sourceLength) {
        return -1;
    }
}
