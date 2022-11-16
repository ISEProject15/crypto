package project.lib.protocol;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface MetaMessage {
    public abstract sealed class Body permits Body.Mapping, Body.Atom {
        public static final int ATOM = 0;
        public static final int MAPPING = 1;

        public final int TYPE;

        protected Body(int type) {
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

        // an interface represents mapping object like { key0: value0, key1: value1 }.
        public static non-sealed abstract class Mapping extends Body {
            protected Mapping() {
                super(Body.MAPPING);
            }

            public abstract Body get(String key);

            public abstract Set<Map.Entry<String, Body>> entries();
        }

        // an interface represents text
        public static non-sealed abstract class Atom extends Body {
            protected Atom() {
                super(Body.ATOM);
            }

            public abstract String text();
        }
    }

    public String identity();

    public Body body();
}
