package project.lib;

public interface StringDecoder<T> {
    public T decode(CharSequence sequence);
}
