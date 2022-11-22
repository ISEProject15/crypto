package project.lib.protocol;

import java.util.ArrayList;
import java.util.HashMap;

import project.lib.scaffolding.collections.SegmentBuffer;

public class IonBuilder {
    public static Associations assoc(String id, String atom) {
        return (builder) -> {
            builder.set(id, atom);
        };
    }

    public static Associations assoc(String id, Ion ion) {
        return (builder) -> {
            builder.set(id, ion);
        };
    }

    public static Associations assoc(String id, Associations associations) {
        return (builder) -> {
            final var b = new MappingBuilder();
            associations.apply(builder);
            builder.set(id, b.build());
        };
    }

    public static Ion create(Associations associations) {
        final var builder = new MappingBuilder();
        associations.apply(builder);
        return builder.build();
    }

    @FunctionalInterface
    public static interface Associations {
        public void apply(MappingBuilder builder);

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
    }

    @FunctionalInterface
    public static interface List {
        public void apply(ArrayBuilder builder);

        public default List add(Ion ion) {
            return (builder) -> {
                builder.add(ion);
            };
        }

    }

    private static class ArrayBuilder {
        final Ion[] temp = new Ion[1];
        SegmentBuffer<Ion[]> buffer = new SegmentBuffer<>(Ion[].class);

        public ArrayBuilder add(Ion ion) {
            temp[0] = ion;
            this.buffer.write(temp, 1);
            return this;
        }

        public Ion.Array build() {
            return ArrayImpl.of(buffer.toArray());
        }
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
