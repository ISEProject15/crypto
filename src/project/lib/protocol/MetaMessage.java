package project.lib.protocol;

public interface MetaMessage {
    public abstract sealed interface Body permits Body.Mapping, Body.Value, Body.Array {
        // an interface represents mapping object like { key0: value0, key1: value1 }.
        public non-sealed interface Mapping extends Body {
            public Body get(String key);
        }

        // an interface represents text
        public non-sealed interface Value extends Body {
            public String text();
        }

        // an interface represents array object like [ item0, item1 ]
        public non-sealed interface Array extends Body {
            public Body get(int index);

            public int length();
        }
    }

    public String identity();

    public Body body();
}
