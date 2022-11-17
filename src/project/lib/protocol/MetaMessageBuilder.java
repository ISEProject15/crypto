package project.lib.protocol;

import java.util.HashMap;

public class MetaMessageBuilder {
    public static Associations assoc(String id, String atom) {
        return (builder) -> {
            builder.set(id, atom);
        };
    }

    public static Associations assoc(String id, Associations associations) {
        return (builder) -> {
            final var b = new MappingBuilder();
            associations.apply(builder);
            builder.set(id, b.build());
        };
    }

    public static MetaMessage create(String id, Associations associations) {
        final var builder = new MappingBuilder();
        associations.apply(builder);
        final var mapping = builder.build();
        return MetaMsg.of(id, mapping);
    }

    public static MetaMessage create(String id, String atom) {
        return MetaMsg.of(id, AtomImpl.of(atom));
    }

    public static MetaMessage create(String id, MetaMessage.Body body) {
        return MetaMsg.of(id, body);
    }

    @FunctionalInterface
    public static interface Associations {
        public default Associations assoc(String key, Associations association) {
            return (builder) -> {
                this.apply(builder);
                final var b = new MappingBuilder();
                association.apply(b);
                builder.set(key, b.build());
            };
        }

        public default Associations assoc(String key, String value) {
            return (builder) -> {
                this.apply(builder);
                builder.set(key, value);
            };
        }

        public default Associations assoc(String key, MetaMessage.Body value) {
            return (builder) -> {
                this.apply(builder);
                builder.set(key, value);
            };
        }

        public void apply(MappingBuilder builder);
    }

    private static class MappingBuilder {
        private final HashMap<String, MetaMessage.Body> map;

        MappingBuilder() {
            this.map = new HashMap<>();
        }

        public MappingBuilder set(String key, String value) {
            return this.set(key, AtomImpl.of(value));
        }

        public MappingBuilder set(String key, MetaMessage.Body value) {
            this.map.put(key, value);
            return this;
        }

        public MetaMessage.Body.Mapping build() {
            return new MappingImpl(map);
        }
    }
}
