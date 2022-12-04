package project.lib.protocol;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import project.lib.scaffolding.dynamic.DynamicObjectView;

public abstract sealed class Ion permits Ion.Mapping, Ion.Atom, Ion.Array {
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

    public final Array asArray() {
        return (Array) this;
    }

    public abstract DynamicObjectView dynamicView();

    // an interface represents mapping object like { key0: value0, key1: value1 }
    public static non-sealed abstract class Mapping extends Ion {
        protected Mapping() {
            super(Ion.MAPPING);
        }

        public abstract Ion get(String key);

        public abstract Set<Map.Entry<String, Ion>> entries();

        @Override
        public final DynamicObjectView dynamicView() {
            return new DynamicObjectView() {
                {
                    this.mapping = Mapping.this;
                }

                final Mapping mapping;

                @Override
                public DynamicObjectView prop(String identity) {
                    final var p = this.mapping.get(identity);
                    if (p == null) {
                        return null;
                    }
                    return p.dynamicView();
                }
            };
        }
    }

    // an interface represents text
    public static non-sealed abstract class Atom extends Ion {
        protected Atom() {
            super(Ion.ATOM);
        }

        public abstract String text();

        @Override
        public final DynamicObjectView dynamicView() {
            return new DynamicObjectView() {
                {
                    this.atom = Atom.this;
                }

                final Atom atom;

                @Override
                @SuppressWarnings("unchecked")
                public <U> U as(Class<U> cls) {
                    if (cls != String.class) {
                        return null;
                    }
                    return (U) this.atom.text();
                }

                @Override
                public <U> boolean is(Class<U> cls) {
                    return cls == String.class;
                }
            };
        }
    }

    // an interface represents array object like [ "a", "b" ]
    public static non-sealed abstract class Array extends Ion implements Iterable<Ion> {
        protected Array() {
            super(Ion.ARRAY);
        }

        public abstract Ion get(int index);

        public abstract int length();

        @Override
        public Iterator<Ion> iterator() {
            return IntStream.range(0, this.length()).mapToObj(this::get).iterator();
        }

        @Override
        public final DynamicObjectView dynamicView() {
            return new DynamicObjectView() {
                {
                    this.array = Array.this;
                }

                final Array array;

                @Override
                public DynamicObjectView index(int index) {
                    final var item = array.get(index);
                    if (item == null) {
                        return null;
                    }
                    return item.dynamicView();
                }
            };
        }
    }
}
