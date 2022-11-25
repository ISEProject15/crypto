package project.lib.protocol;

import java.io.IOException;

public interface StringEncoder<T> {
    public void encode(Appendable buffer, T value) throws IOException;

    public default CharSequence encode(T value) {
        try {
            final var buffer = new StringBuilder();
            this.encode(buffer, value);
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }
}
