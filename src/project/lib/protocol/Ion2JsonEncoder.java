package project.lib.protocol;

import java.io.IOException;

public class Ion2JsonEncoder implements StringEncoder<Ion> {
    public static final Ion2JsonEncoder instance = new Ion2JsonEncoder();

    private Ion2JsonEncoder() {

    }

    @Override
    public void encode(Appendable buffer, Ion value) throws IOException {
        switch (value.TYPE) {
            case Ion.ATOM -> {
                buffer.append('"').append(value.asAtom().text()).append('"');
            }
            case Ion.MAPPING -> {
                buffer.append('{');
                var initial = true;
                for (final var entry : value.asMapping().entries()) {
                    if (initial) {
                        initial = false;
                    } else {
                        buffer.append(',');
                    }
                    buffer.append('"').append(entry.getKey()).append('"').append(':');
                    encode(buffer, entry.getValue());
                }
                buffer.append('}');
            }

            case Ion.ARRAY -> {
                buffer.append('[');
                var initial = true;
                for (final var item : value.asArray()) {
                    if (initial) {
                        initial = false;
                    } else {
                        buffer.append(',');
                    }
                    encode(buffer, item);
                }
                buffer.append(']');
            }
        }
    }

}
