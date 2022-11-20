package project.lib.protocol;

public interface Serializer<T> {
    public T deserialize(CharSequence sequence);

    public void serialize(Appendable buffer, T value);
}
