package project.lib.protocol;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import project.lib.protocol.IonBuilder.Associations;

public abstract sealed class Ion permits Ion.Mapping, Ion.Atom, Ion.Array {
    public static Ion of(Associations associations) {
        return IonBuilder.create(associations);
    }

    public static Ion of(String text) {
        return AtomImpl.of(text);
    }

    public static final int ATOM = 0;
    public static final int MAPPING = 1;
    public static final int ARRAY = 2;

    public final int TYPE;

    protected Ion(int type) {
        this.TYPE = type;
    }

    public final Atom asAtom() {
        return (Atom) this;
    }

    public final Mapping asMapping() {
        return (Mapping) this;
    }

    public final <T> T match(Function<Atom, T> onAtom, Function<Mapping, T> onMapping) {
        return switch (this.TYPE) {
            case ATOM -> onAtom.apply(this.asAtom());
            case MAPPING -> onMapping.apply(this.asMapping());
            default -> throw new IllegalStateException();
        };
    }

    // an interface represents mapping object like { key0: value0, key1: value1 }
    public static non-sealed abstract class Mapping extends Ion {
        protected Mapping() {
            super(Ion.MAPPING);
        }

        public abstract Ion get(String key);

        public abstract Set<Map.Entry<String, Ion>> entries();
    }

    // an interface represents text
    public static non-sealed abstract class Atom extends Ion {
        protected Atom() {
            super(Ion.ATOM);
        }

        public abstract String text();
    }
    // an interface represents array object like [ "a", "b" ]
    public static non-sealed abstract class Array extends Ion {
        protected Array() {
            super(Ion.Array);
        }

        public abstract Ion get(int index);
        public abstract int length();
    }
}
