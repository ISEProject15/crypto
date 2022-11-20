package project.lib.protocol;

import java.util.HashMap;

public class IonBuilder {
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

    public static Ion create(String atom) {
        return AtomImpl.of(atom);
    }

    public static Ion create(Associations associations) {
        final var builder = new MappingBuilder();
        associations.apply(builder);
        return builder.build();
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

        public default Associations assoc(String key, Ion value) {
            return (builder) -> {
                this.apply(builder);
                builder.set(key, value);
            };
        }

        public void apply(MappingBuilder builder);
    }

    private static class MappingBuilder {
        private final HashMap<String, Ion> map;

        MappingBuilder() {
            this.map = new HashMap<>();
        }

        public MappingBuilder set(String key, String value) {
            return this.set(key, AtomImpl.of(value));
        }

        public MappingBuilder set(String key, Ion value) {
            this.map.put(key, value);
            return this;
        }

        public Ion.Mapping build() {
            return new MappingImpl(map);
        }
    }
}
