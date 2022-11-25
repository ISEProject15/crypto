package project.lib.protocol;

public interface StringDecoder<T> {
    public T decode(CharSequence sequence);
}
