package project.lib.protocol;

import java.util.Map;

public abstract sealed class MessageBody permits MessageBody.Mapping, MessageBody.Value, MessageBody.Array {
    public final static class Mapping extends MessageBody {
        final Map<String, MessageBody> map;

        Mapping(Map<String, MessageBody> map) {
            this.map = map;
        }

        public MessageBody get(String key) {
            return this.map.get(key);
        }
    }

    public final static class Value extends MessageBody {
        public final String text;

        Value(String text) {
            this.text = text;
        }
    }

    public final static class Array extends MessageBody {
        public final String[] array;

        Array(String[] array) {
            this.array = array;
        }
    }
}
