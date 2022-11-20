package project.lib.protocol;

public interface StringSerializer<T> {
    public T deserialize(CharSequence sequence);

    public void serialize(Appendable buffer, T value);

    public default CharSequence serialize(T value) {
        final var buffer = new StringBuilder();
        this.serialize(buffer, value);
        return buffer;
    }
}
