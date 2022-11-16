package project.lib.protocol;

public interface MetaMessage {
    public abstract sealed interface Body permits Body.Mapping, Body.Atom {
        // an interface represents mapping object like { key0: value0, key1: value1 }.
        public non-sealed interface Mapping extends Body {
            public Body get(String key);
        }

        // an interface represents text
        public non-sealed interface Atom extends Body {
            public String text();
        }
    }

    public String identity();

    public Body body();
}
