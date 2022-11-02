package project.lib;

public interface Transformer {
    // sourceに渡されたブロックの変換を行い，destinationに書き込む．
    // sourceLengthはsource内のバイト数かそのnotが入っており，sourceLengthが負のときは入力の最後の末尾であることを示す．
    // 戻り値はdestinationに書き込んだバイト数かそのnotであり，戻り値が負のときは変換が終了したことを示す．
    public int transform(int sourceLength, byte[] source, byte[] destination);
}
