package project.lib.protocol;

import java.io.IOException;

public interface StringSerializer<T> {
    public T deserialize(CharSequence sequence);

    public void serialize(Appendable buffer, T value) throws IOException;

    public default CharSequence serialize(T value) {
        try {
            final var buffer = new StringBuilder();
            this.serialize(buffer, value);
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }
}
